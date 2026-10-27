package com.m10.everystory.data.news

import com.m10.everystory.core.common.AppError
import com.m10.everystory.core.common.AppResult
import com.m10.everystory.data.news.local.ArticleEntity
import com.m10.everystory.data.news.remote.NewsApiService
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.testing.FakeArticleDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNewsRepositoryTest {

    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private lateinit var repository: DefaultNewsRepository
    private val dao = FakeArticleDao()
    private val now = Instant.parse("2026-10-26T09:30:00Z")

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().readTimeout(1, TimeUnit.SECONDS).build()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/v2/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create<NewsApiService>()
        repository = DefaultNewsRepository(api, dao, json, UnconfinedTestDispatcher(), now = { now })
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `headlines are mapped and removed articles dropped`() = runTest {
        server.enqueue(MockResponse().setBody(HEADLINES_JSON))

        val page = repository.topHeadlines(page = 1, category = NewsCategory.Health).successValue()

        assertEquals(1, page.articles.size)
        val article = page.articles.single()
        assertEquals("Seahawks hand Chiefs first loss", article.title)
        assertEquals("ESPN", article.source.name)
        assertEquals("Seattle's defense was too much.", article.content)
        assertFalse(page.isLastPage)

        val request = server.takeRequest()
        assertEquals("/v2/top-headlines", request.requestUrl?.encodedPath)
        assertEquals("health", request.requestUrl?.queryParameter("category"))
        assertEquals("en", request.requestUrl?.queryParameter("language"))
        assertEquals("20", request.requestUrl?.queryParameter("pageSize"))
    }

    @Test
    fun `loaded articles are cached and can be read back by key`() = runTest {
        server.enqueue(MockResponse().setBody(HEADLINES_JSON))

        val article = repository.topHeadlines(page = 1).successValue().articles.single()

        assertEquals(article, repository.article(article.key))
    }

    @Test
    fun `cache drops entries older than a week`() = runTest {
        dao.rows["old"] = staleEntity()
        server.enqueue(MockResponse().setBody(HEADLINES_JSON))

        repository.topHeadlines(page = 1)

        assertNull(dao.rows["old"])
    }

    @Test
    fun `a failing cache write still returns the stories`() = runTest {
        dao.failWrites = true
        server.enqueue(MockResponse().setBody(HEADLINES_JSON))

        assertEquals(1, repository.topHeadlines(page = 1).successValue().articles.size)
    }

    @Test
    fun `unknown key reads as null`() = runTest {
        assertNull(repository.article("missing"))
    }

    @Test
    fun `last page is detected from totalResults`() = runTest {
        server.enqueue(MockResponse().setBody(HEADLINES_JSON.replace("\"totalResults\": 45", "\"totalResults\": 30")))

        val page = repository.topHeadlines(page = 2).successValue()

        assertTrue(page.isLastPage)
    }

    @Test
    fun `search trims the query and asks for newest first`() = runTest {
        server.enqueue(MockResponse().setBody(HEADLINES_JSON))

        repository.searchArticles(query = "  night shift  ", page = 1)

        val url = server.takeRequest().requestUrl
        assertEquals("/v2/everything", url?.encodedPath)
        assertEquals("night shift", url?.queryParameter("q"))
        assertEquals("publishedAt", url?.queryParameter("sortBy"))
        assertEquals("title,description", url?.queryParameter("searchIn"))
    }

    @Test
    fun `invalid api key maps to Unauthorized`() = runTest {
        server.enqueue(errorResponse(401, "apiKeyInvalid"))

        assertEquals(AppError.Unauthorized, repository.topHeadlines(page = 1).failureError())
    }

    @Test
    fun `rate limit maps to RateLimited`() = runTest {
        server.enqueue(errorResponse(429, "rateLimited"))

        assertEquals(AppError.RateLimited, repository.topHeadlines(page = 1).failureError())
    }

    @Test
    fun `server error maps to Server`() = runTest {
        server.enqueue(errorResponse(500, "unexpectedError"))

        assertEquals(AppError.Server, repository.topHeadlines(page = 1).failureError())
    }

    @Test
    fun `426 past the result cap is the end of the list`() = runTest {
        server.enqueue(errorResponse(426, "maximumResultsReached"))

        val page = repository.searchArticles(query = "levee", page = 6).successValue()

        assertTrue(page.articles.isEmpty())
        assertTrue(page.isLastPage)
    }

    @Test
    fun `no response maps to Timeout`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        assertEquals(AppError.Timeout, repository.topHeadlines(page = 1).failureError())
    }

    @Test
    fun `dropped connection maps to Offline`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(AppError.Offline, repository.topHeadlines(page = 1).failureError())
    }

    private fun staleEntity() = ArticleEntity(
        articleKey = "old",
        title = "Old",
        description = null,
        content = null,
        url = "https://example.com/old",
        imageUrl = null,
        sourceId = null,
        sourceName = "Old Post",
        author = null,
        publishedAtMillis = 0,
        cachedAt = now.minus(Duration.ofDays(8)).toEpochMilli(),
    )

    private fun errorResponse(code: Int, apiCode: String) = MockResponse()
        .setResponseCode(code)
        .setBody("""{"status":"error","code":"$apiCode","message":"test"}""")

    @Test
    fun `other languages read headlines from that language's outlets`() = runTest {
        server.enqueue(MockResponse().setBody(GERMAN_SOURCES_JSON))
        server.enqueue(MockResponse().setBody(HEADLINES_JSON))
        server.enqueue(MockResponse().setBody(HEADLINES_JSON))

        repository.topHeadlines(page = 1, language = NewsLanguage.German).successValue()
        repository.topHeadlines(page = 2, language = NewsLanguage.German).successValue()

        val sourcesRequest = server.takeRequest()
        assertEquals("/v2/top-headlines/sources", sourcesRequest.requestUrl?.encodedPath)
        assertEquals("de", sourcesRequest.requestUrl?.queryParameter("language"))
        // NewsAPI refuses sources mixed with language or category, so only sources is sent.
        val headlines = server.takeRequest().requestUrl
        assertEquals("/v2/top-headlines", headlines?.encodedPath)
        assertEquals("spiegel-online,die-zeit", headlines?.queryParameter("sources"))
        assertNull(headlines?.queryParameter("language"))
        assertNull(headlines?.queryParameter("category"))
        // Page two reuses the outlet list instead of asking again.
        assertEquals("/v2/top-headlines", server.takeRequest().requestUrl?.encodedPath)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `a topic with no outlets in that language is an empty last page`() = runTest {
        server.enqueue(MockResponse().setBody("""{"status": "ok", "sources": []}"""))

        val page = repository.topHeadlines(page = 1, category = NewsCategory.Science, language = NewsLanguage.Arabic)
            .successValue()

        assertTrue(page.articles.isEmpty())
        assertTrue(page.isLastPage)
        assertEquals("science", server.takeRequest().requestUrl?.queryParameter("category"))
        assertEquals(1, server.requestCount)
    }

    private fun AppResult<ArticlePage>.successValue(): ArticlePage =
        (this as? AppResult.Success)?.data ?: error("Expected success but was $this")

    private fun AppResult<*>.failureError(): AppError =
        (this as? AppResult.Failure)?.error ?: error("Expected failure but was $this")

    private companion object {
        val GERMAN_SOURCES_JSON = """
            {"status": "ok", "sources": [
              {"id": "spiegel-online", "name": "Spiegel Online", "category": "general", "language": "de", "country": "de"},
              {"id": "die-zeit", "name": "Die Zeit", "category": "business", "language": "de", "country": "de"}
            ]}
        """.trimIndent()

        val HEADLINES_JSON = """
            {
              "status": "ok",
              "totalResults": 45,
              "articles": [
                {
                  "source": { "id": "espn", "name": "ESPN" },
                  "author": "Nate Taylor",
                  "title": "Seahawks hand Chiefs first loss - ESPN",
                  "description": "Seattle's defense was too much.",
                  "url": "https://www.espn.com/nfl/story/1",
                  "urlToImage": "https://a.espncdn.com/photo.jpg",
                  "publishedAt": "2026-10-26T04:18:10Z",
                  "content": "Seattle's defense was too much. [+6057 chars]"
                },
                {
                  "source": { "id": null, "name": "[Removed]" },
                  "title": "[Removed]",
                  "url": "https://removed.com",
                  "publishedAt": "2026-10-26T04:00:00Z"
                }
              ]
            }
        """.trimIndent()
    }
}

package com.m10.everystory.data.news

import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.network.toAppError
import com.m10.everystory.data.news.local.ArticleDao
import com.m10.everystory.data.news.local.FeedDao
import com.m10.everystory.data.news.local.FeedEntryEntity
import com.m10.everystory.data.news.mapper.toArticlePage
import com.m10.everystory.data.news.mapper.toDomain
import com.m10.everystory.data.news.mapper.toEntity
import com.m10.everystory.data.news.remote.NewsApiService
import com.m10.everystory.data.news.remote.dto.ArticlesResponseDto
import com.m10.everystory.data.news.remote.dto.NewsApiErrorDto
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.CachedHeadlines
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.NewsSource
import com.m10.everystory.domain.repository.MAX_RESULTS
import com.m10.everystory.domain.repository.NewsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber
import java.time.Duration
import java.time.Instant

const val PAGE_SIZE = 20

private const val MAX_QUERY_LENGTH = 500
private const val SORT_NEWEST_FIRST = "publishedAt"

// Matching the whole article body brings back stories that only mention the word in passing.
private const val SEARCH_IN_TITLE_AND_DESCRIPTION = "title,description"
private const val HTTP_UPGRADE_REQUIRED = 426

// NewsAPI accepts at most 20 sources in one request.
private const val MAX_SOURCES = 20

// Long enough to reopen anything from the last week, short enough that the table stays small.
private val CACHE_TTL = Duration.ofDays(7)

class DefaultNewsRepository(
    private val api: NewsApiService,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val now: () -> Instant = Instant::now,
) : NewsRepository {

    // Outlet ids per language and topic. They rarely change, so we ask once per app run.
    private val sourceIds = mutableMapOf<Pair<NewsLanguage, NewsCategory?>, List<String>>()
    private val sourceIdsMutex = Mutex()

    override suspend fun topHeadlines(
        page: Int,
        category: NewsCategory?,
        language: NewsLanguage,
    ): AppResult<ArticlePage> {
        val result = fetchHeadlines(page, category, language)
        // Page one is what Today shows first, so it's kept for opening the app without a connection.
        if (page == 1 && result is AppResult.Success) {
            rememberFeed(feedKey(category, language), result.data.articles)
        }
        return result
    }

    override suspend fun cachedHeadlines(
        category: NewsCategory?,
        language: NewsLanguage,
    ): CachedHeadlines? = withContext(ioDispatcher) {
        val key = feedKey(category, language)
        val articles = feedDao.articles(key)
        val fetchedAt = feedDao.fetchedAt(key)
        if (articles.isEmpty() || fetchedAt == null) {
            null
        } else {
            CachedHeadlines(articles.map { it.toDomain() }, Instant.ofEpochMilli(fetchedAt))
        }
    }

    private suspend fun fetchHeadlines(
        page: Int,
        category: NewsCategory?,
        language: NewsLanguage,
    ): AppResult<ArticlePage> {
        if (language == NewsLanguage.English) {
            return fetchPage(page) {
                api.topHeadlines(
                    language = language.code,
                    category = category?.apiValue,
                    sources = null,
                    page = page,
                    pageSize = PAGE_SIZE,
                )
            }
        }
        // On the free plan, top headlines filtered by language only work for English. Other
        // languages come from that language's outlets, narrowed to the topic by outlet category.
        val ids = when (val result = sourceIdsFor(language, category)) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> return result
        }
        if (ids.isEmpty()) {
            return AppResult.Success(ArticlePage(articles = emptyList(), page = page, totalResults = 0, isLastPage = true))
        }
        return fetchPage(page) {
            api.topHeadlines(
                language = null,
                category = null,
                sources = ids.joinToString(","),
                page = page,
                pageSize = PAGE_SIZE,
            )
        }
    }

    override suspend fun searchArticles(
        query: String,
        page: Int,
        language: NewsLanguage,
    ): AppResult<ArticlePage> = fetchPage(page) {
        api.everything(
            query = query.trim().take(MAX_QUERY_LENGTH),
            searchIn = SEARCH_IN_TITLE_AND_DESCRIPTION,
            language = language.code,
            sortBy = SORT_NEWEST_FIRST,
            page = page,
            pageSize = PAGE_SIZE,
        )
    }

    override suspend fun sources(
        category: NewsCategory?,
        language: NewsLanguage,
    ): AppResult<List<NewsSource>> = safeApiCall {
        api.sources(category = category?.apiValue, language = language.code).sources.map { it.toDomain() }
    }

    override suspend fun checkApiKey(key: String): AppResult<Unit> = safeApiCall {
        api.checkKey(apiKey = key, language = NewsLanguage.English.code)
        Unit
    }

    override suspend fun article(key: String): Article? = withContext(ioDispatcher) {
        articleDao.getByKey(key)?.toDomain()
    }

    private suspend fun sourceIdsFor(language: NewsLanguage, category: NewsCategory?): AppResult<List<String>> =
        sourceIdsMutex.withLock {
            sourceIds[language to category]?.let { return AppResult.Success(it) }
            when (val result = sources(category, language)) {
                is AppResult.Success -> {
                    val ids = result.data.map { it.id }.take(MAX_SOURCES)
                    sourceIds[language to category] = ids
                    AppResult.Success(ids)
                }
                is AppResult.Failure -> result
            }
        }

    private suspend fun fetchPage(
        page: Int,
        request: suspend () -> ArticlesResponseDto,
    ): AppResult<ArticlePage> = safeApiCall {
        try {
            request().toArticlePage(page, PAGE_SIZE, MAX_RESULTS).also { cache(it.articles) }
        } catch (e: HttpException) {
            // Asking past result 100 on the developer plan returns 426 maximumResultsReached.
            // For the reader that's simply the end of the list, not an error.
            if (e.code() != HTTP_UPGRADE_REQUIRED) throw e
            ArticlePage(articles = emptyList(), page = page, totalResults = MAX_RESULTS, isLastPage = true)
        }
    }

    // Caching is a convenience; if the database write fails, the reader still gets their stories.
    private suspend fun cache(articles: List<Article>) {
        if (articles.isEmpty()) return
        val cachedAt = now()
        try {
            articleDao.upsertAll(articles.map { it.toEntity(cachedAt) })
            articleDao.deleteCachedBefore(cachedAt.minus(CACHE_TTL).toEpochMilli())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Couldn't cache %d articles", articles.size)
        }
    }

    // Like the article cache, a failed write here never costs the reader their stories.
    private suspend fun rememberFeed(feedKey: String, articles: List<Article>) = withContext(ioDispatcher) {
        val fetchedAt = now()
        try {
            feedDao.replaceFeed(
                feedKey,
                articles.mapIndexed { index, article ->
                    FeedEntryEntity(feedKey, index, article.key, fetchedAt.toEpochMilli())
                },
            )
            feedDao.deleteFetchedBefore(fetchedAt.minus(CACHE_TTL).toEpochMilli())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Couldn't remember feed %s", feedKey)
        }
    }

    private fun feedKey(category: NewsCategory?, language: NewsLanguage) =
        "${language.code}/${category?.apiValue ?: "all"}"

    private suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> =
        withContext(ioDispatcher) {
            try {
                AppResult.Success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val error = e.toAppError()
                Timber.w(e, "News request failed: %s %s", error, newsApiCode(e).orEmpty())
                AppResult.Failure(error)
            }
        }

    private fun newsApiCode(e: Exception): String? {
        val body = (e as? HttpException)?.response()?.errorBody()?.string() ?: return null
        return runCatching { json.decodeFromString<NewsApiErrorDto>(body).code }.getOrNull()
    }
}

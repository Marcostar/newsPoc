package com.m10.everystory.data.news

import com.m10.everystory.core.common.AppResult
import com.m10.everystory.core.network.toAppError
import com.m10.everystory.data.news.local.ArticleDao
import com.m10.everystory.data.news.mapper.toArticlePage
import com.m10.everystory.data.news.mapper.toDomain
import com.m10.everystory.data.news.mapper.toEntity
import com.m10.everystory.data.news.remote.NewsApiService
import com.m10.everystory.data.news.remote.dto.ArticlesResponseDto
import com.m10.everystory.data.news.remote.dto.NewsApiErrorDto
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.model.ArticlePage
import com.m10.everystory.domain.model.NewsCategory
import com.m10.everystory.domain.model.NewsSource
import com.m10.everystory.domain.repository.MAX_RESULTS
import com.m10.everystory.domain.repository.NewsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

// Long enough to reopen anything from the last week, short enough that the table stays small.
private val CACHE_TTL = Duration.ofDays(7)

class DefaultNewsRepository(
    private val api: NewsApiService,
    private val articleDao: ArticleDao,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val now: () -> Instant = Instant::now,
) : NewsRepository {

    override suspend fun topHeadlines(
        page: Int,
        category: NewsCategory?,
        language: String,
    ): AppResult<ArticlePage> = fetchPage(page) {
        api.topHeadlines(
            language = language,
            category = category?.apiValue,
            page = page,
            pageSize = PAGE_SIZE,
        )
    }

    override suspend fun searchArticles(
        query: String,
        page: Int,
        language: String,
    ): AppResult<ArticlePage> = fetchPage(page) {
        api.everything(
            query = query.trim().take(MAX_QUERY_LENGTH),
            searchIn = SEARCH_IN_TITLE_AND_DESCRIPTION,
            language = language,
            sortBy = SORT_NEWEST_FIRST,
            page = page,
            pageSize = PAGE_SIZE,
        )
    }

    override suspend fun sources(
        category: NewsCategory?,
        language: String,
    ): AppResult<List<NewsSource>> = safeApiCall {
        api.sources(category = category?.apiValue, language = language).sources.map { it.toDomain() }
    }

    override suspend fun article(key: String): Article? = withContext(ioDispatcher) {
        articleDao.getByKey(key)?.toDomain()
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

package com.m10.everystory.data.saved

import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.repository.SavedArticlesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RoomSavedArticlesRepository(
    private val savedArticleDao: SavedArticleDao,
    private val now: () -> Instant = Instant::now,
) : SavedArticlesRepository {

    override val savedArticles: Flow<List<Article>> =
        savedArticleDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun isSaved(key: String): Flow<Boolean> = savedArticleDao.observeIsSaved(key)

    override suspend fun savedArticle(key: String): Article? = savedArticleDao.getByKey(key)?.toDomain()

    override suspend fun save(article: Article) = savedArticleDao.upsert(article.toSavedEntity(now()))

    override suspend fun remove(key: String) = savedArticleDao.delete(key)
}

package com.m10.everystory.testing

import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.repository.SavedArticlesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSavedArticlesRepository : SavedArticlesRepository {

    // Newest first, like the real query.
    private val saved = MutableStateFlow<List<Article>>(emptyList())

    override val savedArticles: Flow<List<Article>> get() = saved

    override fun isSaved(key: String): Flow<Boolean> = saved.map { list -> list.any { it.key == key } }

    override suspend fun savedArticle(key: String): Article? = saved.value.firstOrNull { it.key == key }

    override suspend fun save(article: Article) {
        saved.value = listOf(article) + saved.value.filterNot { it.key == article.key }
    }

    override suspend fun remove(key: String) {
        saved.value = saved.value.filterNot { it.key == key }
    }
}

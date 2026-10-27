package com.m10.everystory.feature.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.m10.everystory.EveryStoryApplication
import com.m10.everystory.core.analytics.AnalyticsEvent
import com.m10.everystory.core.analytics.AnalyticsTracker
import com.m10.everystory.core.notifications.SavedReminderScheduler
import com.m10.everystory.domain.model.Article
import com.m10.everystory.domain.repository.NewsRepository
import com.m10.everystory.domain.repository.SavedArticlesRepository
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StoryUiState(
    val content: StoryContent = StoryContent.Loading,
    val isSaved: Boolean = false,
    val message: StoryMessage? = null,
    // One-off: the screen asks Android for notification permission, then reports back.
    val askNotificationPermission: Boolean = false,
)

sealed interface StoryContent {
    data object Loading : StoryContent
    data class Ready(val article: Article) : StoryContent
    data object NotFound : StoryContent
}

enum class StoryMessage { Saved, Removed }

class StoryViewModel(
    private val articleKey: String,
    private val newsRepository: NewsRepository,
    private val savedArticlesRepository: SavedArticlesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedReminders: SavedReminderScheduler,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val content = MutableStateFlow<StoryContent>(StoryContent.Loading)
    private val message = MutableStateFlow<StoryMessage?>(null)
    private val askNotificationPermission = MutableStateFlow(false)

    val uiState: StateFlow<StoryUiState> =
        combine(
            content,
            savedArticlesRepository.isSaved(articleKey),
            message,
            askNotificationPermission,
        ) { content, isSaved, message, askPermission ->
            StoryUiState(content = content, isSaved = isSaved, message = message, askNotificationPermission = askPermission)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), StoryUiState())

    init {
        viewModelScope.launch {
            // The cache is pruned after a week; a saved story keeps its own copy, so try that next.
            val article = newsRepository.article(articleKey) ?: savedArticlesRepository.savedArticle(articleKey)
            content.value = article?.let { StoryContent.Ready(it) } ?: StoryContent.NotFound
            article?.let { analytics.log(AnalyticsEvent.StoryOpen(it.source.name)) }
        }
    }

    fun onToggleSave() {
        val article = (content.value as? StoryContent.Ready)?.article ?: return
        viewModelScope.launch {
            if (savedArticlesRepository.isSaved(articleKey).first()) {
                savedArticlesRepository.remove(articleKey)
                message.value = StoryMessage.Removed
                analytics.log(AnalyticsEvent.StoryUnsave(article.source.name))
            } else {
                savedArticlesRepository.save(article)
                message.value = StoryMessage.Saved
                analytics.log(AnalyticsEvent.StorySave(article.source.name))
                savedReminders.schedule()
                // The first save is when a reminder starts to make sense, so that's when we ask.
                if (!userPreferencesRepository.notificationPermissionAsked.first()) {
                    askNotificationPermission.value = true
                }
            }
        }
    }

    fun onUndo(undone: StoryMessage) {
        val article = (content.value as? StoryContent.Ready)?.article ?: return
        viewModelScope.launch {
            when (undone) {
                StoryMessage.Saved -> savedArticlesRepository.remove(articleKey)
                StoryMessage.Removed -> savedArticlesRepository.save(article)
            }
        }
    }

    // The Custom Tab itself is opened by the screen; it needs a Context.
    fun onReadFullStory() {
        val article = (content.value as? StoryContent.Ready)?.article ?: return
        analytics.log(AnalyticsEvent.StoryLinkOut(article.source.name))
    }

    fun onNotificationPermissionAsked() {
        askNotificationPermission.value = false
        viewModelScope.launch { userPreferencesRepository.setNotificationPermissionAsked() }
    }

    fun onMessageShown() {
        message.value = null
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        fun factory(articleKey: String) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EveryStoryApplication
                StoryViewModel(
                    articleKey = articleKey,
                    newsRepository = app.container.newsRepository,
                    savedArticlesRepository = app.container.savedArticlesRepository,
                    userPreferencesRepository = app.container.userPreferencesRepository,
                    savedReminders = app.container.savedReminderScheduler,
                    analytics = app.container.analyticsTracker,
                )
            }
        }
    }
}

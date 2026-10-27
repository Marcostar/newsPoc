package com.m10.everystory.testing

import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.TextSize
import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

class FakeUserPreferencesRepository(tourSeen: Boolean = false) : UserPreferencesRepository {

    private val theme = MutableStateFlow(ThemeSetting.System)
    override val themeSetting: StateFlow<ThemeSetting> get() = theme

    override suspend fun setThemeSetting(setting: ThemeSetting) {
        theme.value = setting
    }

    private val textSize = MutableStateFlow(TextSize.DEFAULT_STEP)
    override val textSizeStep: StateFlow<Int> get() = textSize

    override suspend fun setTextSizeStep(step: Int) {
        textSize.value = step
    }

    private val language = MutableStateFlow(NewsLanguage.Default)
    override val newsLanguage: StateFlow<NewsLanguage> get() = language

    override suspend fun setNewsLanguage(language: NewsLanguage) {
        this.language.value = language
    }

    private val lastViewed = MutableStateFlow(Instant.EPOCH)
    override val savedLastViewedAt: StateFlow<Instant> get() = lastViewed

    override suspend fun setSavedLastViewedAt(time: Instant) {
        lastViewed.value = time
    }

    private val permissionAsked = MutableStateFlow(false)
    override val notificationPermissionAsked: StateFlow<Boolean> get() = permissionAsked

    override suspend fun setNotificationPermissionAsked() {
        permissionAsked.value = true
    }

    private val seen = MutableStateFlow(tourSeen)
    override val tourSeen: StateFlow<Boolean> get() = seen

    override suspend fun setTourSeen() {
        seen.value = true
    }
}

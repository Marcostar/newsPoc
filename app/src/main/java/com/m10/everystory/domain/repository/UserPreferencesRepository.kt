package com.m10.everystory.domain.repository

import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.ThemeSetting
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface UserPreferencesRepository {
    val themeSetting: Flow<ThemeSetting>

    suspend fun setThemeSetting(setting: ThemeSetting)

    // An index into TextSize.scales.
    val textSizeStep: Flow<Int>

    suspend fun setTextSizeStep(step: Int)

    val newsLanguage: Flow<NewsLanguage>

    suspend fun setNewsLanguage(language: NewsLanguage)

    // When the reader last looked at Saved; the reminder only counts stories saved after this.
    val savedLastViewedAt: Flow<Instant>

    suspend fun setSavedLastViewedAt(time: Instant)

    // Android only lets us ask twice, so we ask once, the first time a story is saved.
    val notificationPermissionAsked: Flow<Boolean>

    suspend fun setNotificationPermissionAsked()

    val tourSeen: Flow<Boolean>

    suspend fun setTourSeen()
}

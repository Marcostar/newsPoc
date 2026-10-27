package com.m10.everystory.domain.repository

import com.m10.everystory.domain.model.NewsLanguage
import com.m10.everystory.domain.model.ThemeSetting
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val themeSetting: Flow<ThemeSetting>

    suspend fun setThemeSetting(setting: ThemeSetting)

    // An index into TextSize.scales.
    val textSizeStep: Flow<Int>

    suspend fun setTextSizeStep(step: Int)

    val newsLanguage: Flow<NewsLanguage>

    suspend fun setNewsLanguage(language: NewsLanguage)

    val tourSeen: Flow<Boolean>

    suspend fun setTourSeen()
}

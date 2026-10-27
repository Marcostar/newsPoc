package com.m10.everystory.domain.repository

import com.m10.everystory.domain.model.ThemeSetting
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val themeSetting: Flow<ThemeSetting>

    suspend fun setThemeSetting(setting: ThemeSetting)

    // An index into TextSize.scales.
    val textSizeStep: Flow<Int>

    suspend fun setTextSizeStep(step: Int)

    val tourSeen: Flow<Boolean>

    suspend fun setTourSeen()
}

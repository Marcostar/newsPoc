package com.m10.everystory.testing

import com.m10.everystory.domain.model.ThemeSetting
import com.m10.everystory.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserPreferencesRepository : UserPreferencesRepository {

    private val theme = MutableStateFlow(ThemeSetting.System)
    override val themeSetting: StateFlow<ThemeSetting> get() = theme

    override suspend fun setThemeSetting(setting: ThemeSetting) {
        theme.value = setting
    }
}

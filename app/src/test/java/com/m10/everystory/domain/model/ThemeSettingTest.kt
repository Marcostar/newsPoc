package com.m10.everystory.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSettingTest {

    @Test
    fun `system follows the phone, light and dark ignore it`() {
        assertTrue(ThemeSetting.System.isDark(systemIsDark = true))
        assertFalse(ThemeSetting.System.isDark(systemIsDark = false))
        assertFalse(ThemeSetting.Light.isDark(systemIsDark = true))
        assertTrue(ThemeSetting.Dark.isDark(systemIsDark = false))
    }
}

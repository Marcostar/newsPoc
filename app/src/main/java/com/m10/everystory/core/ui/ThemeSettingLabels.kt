package com.m10.everystory.core.ui

import androidx.annotation.StringRes
import com.m10.everystory.R
import com.m10.everystory.domain.model.ThemeSetting

// Light, Dark, then System, in the prototype's order. Shared by You and the Aa sheet.
val themeOptions = listOf(ThemeSetting.Light, ThemeSetting.Dark, ThemeSetting.System)

@StringRes
fun ThemeSetting.labelRes(): Int = when (this) {
    ThemeSetting.Light -> R.string.theme_light
    ThemeSetting.Dark -> R.string.theme_dark
    ThemeSetting.System -> R.string.theme_system
}

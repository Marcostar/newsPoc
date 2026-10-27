package com.m10.everystory.domain.model

enum class ThemeSetting {
    System,
    Light,
    Dark,
    ;

    fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        System -> systemIsDark
        Light -> false
        Dark -> true
    }
}

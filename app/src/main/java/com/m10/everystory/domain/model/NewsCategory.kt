package com.m10.everystory.domain.model

enum class NewsCategory {
    General,
    Business,
    Entertainment,
    Health,
    Science,
    Sports,
    Technology,
    ;

    val apiValue: String get() = name.lowercase()
}

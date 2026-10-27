package com.m10.everystory.domain.model

// The language of the news itself, separate from the app's own language. A short list for the
// proof of concept, chosen from what NewsAPI's free plan actually has fresh stories for.
enum class NewsLanguage(val code: String) {
    English("en"),
    German("de"),
    French("fr"),
    Arabic("ar"),
    ;

    companion object {
        val Default = English

        fun fromCode(code: String?): NewsLanguage = entries.firstOrNull { it.code == code } ?: Default
    }
}

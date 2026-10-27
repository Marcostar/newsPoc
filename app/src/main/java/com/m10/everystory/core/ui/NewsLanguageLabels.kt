package com.m10.everystory.core.ui

import androidx.annotation.StringRes
import androidx.compose.ui.text.intl.LocaleList
import com.m10.everystory.R
import com.m10.everystory.domain.model.NewsLanguage

// Each language's own name for itself, so readers spot theirs whatever the phone is set to.
@StringRes
fun NewsLanguage.nativeNameRes(): Int = when (this) {
    NewsLanguage.English -> R.string.language_native_en
    NewsLanguage.German -> R.string.language_native_de
    NewsLanguage.French -> R.string.language_native_fr
    NewsLanguage.Arabic -> R.string.language_native_ar
}

@StringRes
fun NewsLanguage.englishNameRes(): Int = when (this) {
    NewsLanguage.English -> R.string.language_english_en
    NewsLanguage.German -> R.string.language_english_de
    NewsLanguage.French -> R.string.language_english_fr
    NewsLanguage.Arabic -> R.string.language_english_ar
}

// Tells TalkBack and the text shaper which language a native name is written in.
fun NewsLanguage.localeList(): LocaleList = LocaleList(code)

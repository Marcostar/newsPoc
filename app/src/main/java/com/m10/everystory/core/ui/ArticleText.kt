package com.m10.everystory.core.ui

import java.net.URI

private const val MAX_INITIALS = 2
private val SKIPPED_WORDS = setOf("the", "a", "an")

// "https://www.northfieldpost.com/x" -> "northfieldpost.com". The design always names the domain.
fun displayDomain(url: String): String =
    runCatching { URI(url).host }.getOrNull()
        ?.removePrefix("www.")
        ?.takeIf { it.isNotBlank() }
        ?: url

// "The Northfield Post" -> "NP". Used for the source badge instead of a logo we don't have.
fun sourceInitials(name: String): String =
    name.split(' ', '-', '.')
        .filter { it.isNotBlank() && it.lowercase() !in SKIPPED_WORDS }
        .take(MAX_INITIALS)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { name.take(1).uppercase() }

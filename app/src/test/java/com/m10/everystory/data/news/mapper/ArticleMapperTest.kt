package com.m10.everystory.data.news.mapper

import com.m10.everystory.data.news.remote.dto.ArticleDto
import com.m10.everystory.data.news.remote.dto.ArticleSourceDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleMapperTest {

    private val dto = ArticleDto(
        source = ArticleSourceDto(id = null, name = "Northfield Post"),
        title = "A river town rewrites its flood plan - Northfield Post",
        url = "https://northfieldpost.com/flood-plan",
        urlToImage = "https://northfieldpost.com/flood.jpg",
        publishedAt = "2026-10-24T07:40:00Z",
    )

    @Test
    fun `title loses the trailing source name`() {
        assertEquals("A river town rewrites its flood plan", dto.toDomain()?.title)
    }

    @Test
    fun `title loses a trailing domain-style source`() {
        assertEquals(
            "Musk Claims SpaceX Closed Debt Deal",
            "Musk Claims SpaceX Closed Debt Deal - Bloomberg.com".withoutSourceSuffix("Bloomberg"),
        )
        assertEquals("Lula is re-elected", "Lula is re-elected - The Wall Street Journal".withoutSourceSuffix("The Wall Street Journal"))
    }

    @Test
    fun `title without the suffix is left alone`() {
        assertEquals("Levee repairs - phase two", "Levee repairs - phase two".withoutSourceSuffix("Metro Herald"))
    }

    @Test
    fun `content loses the truncation marker`() {
        assertEquals("The council voted 7–2…", "The council voted 7–2… [+1840 chars]".withoutTruncationMarker())
    }

    @Test
    fun `html in a summary becomes plain text`() {
        assertEquals(
            "Talks in Tehran aim to stop the war DW.com",
            "<ol><li><a href=\"https://news.google.com/x\">Talks in Tehran aim to stop the war</a>&nbsp;&nbsp;<font color=\"#6f6f6f\">DW.com</font></li></ol>"
                .withoutHtml(),
        )
        assertEquals("Q&A: rates, \"cuts\" & <tags>", "Q&amp;A: rates, &quot;cuts&quot; &amp; &lt;tags&gt;".withoutHtml())
        assertEquals("A plain summary", dto.copy(description = "  A plain summary ").toDomain()?.description)
    }

    @Test
    fun `a title that is only the source name is dropped`() {
        assertNull(dto.copy(source = ArticleSourceDto(id = null, name = "Google News (Saudi Arabia)"), title = "Google News").toDomain())
    }

    @Test
    fun `articles missing a url, title or date are dropped`() {
        assertNull(dto.copy(url = null).toDomain())
        assertNull(dto.copy(title = "[Removed]").toDomain())
        assertNull(dto.copy(publishedAt = "yesterday").toDomain())
    }

    @Test
    fun `non http image urls are ignored`() {
        assertNull(dto.copy(urlToImage = "data:image/png;base64,AAAA").toDomain()?.imageUrl)
    }

    @Test
    fun `article key is stable per url`() {
        val key = articleKey("https://northfieldpost.com/flood-plan")
        assertEquals(key, articleKey("https://northfieldpost.com/flood-plan"))
        assertNotEquals(key, articleKey("https://northfieldpost.com/other"))
        assertEquals(16, key.length)
    }
}

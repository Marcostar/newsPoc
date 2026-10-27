package com.m10.everystory.core.analytics

import com.m10.everystory.core.common.AppError
import com.m10.everystory.domain.model.NewsCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsEventTest {

    @Test
    fun `errors are sent as readable names, not class names`() {
        val event = AnalyticsEvent.LoadError(AnalyticsScreen.Search, AppError.RateLimited)

        assertEquals("load_error", event.name)
        assertEquals(mapOf("screen_name" to "search", "error_type" to "rate_limited"), event.params)
    }

    @Test
    fun `the All chip is sent as all`() {
        assertEquals(mapOf("topic" to "all"), AnalyticsEvent.TopicSelect(category = null).params)
        assertEquals(mapOf("topic" to "health"), AnalyticsEvent.TopicSelect(NewsCategory.Health).params)
    }
}

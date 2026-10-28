package com.m10.everystory.core.ui

import com.m10.everystory.R
import com.m10.everystory.core.common.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorMessageTest {

    @Test
    fun `running out of free requests on the built-in key offers the reader's own key`() {
        val message = AppError.RateLimited.toErrorMessage(usingOwnKey = false)

        assertEquals(ErrorAction.UseOwnKey, message.action)
        assertEquals(R.string.error_rate_limited_body, message.body)
    }

    @Test
    fun `running out on the reader's own key just says try again later`() {
        val message = AppError.RateLimited.toErrorMessage(usingOwnKey = true)

        assertEquals(ErrorAction.TryAgain, message.action)
        assertEquals(R.string.error_own_key_rate_limited_body, message.body)
    }

    @Test
    fun `a rejected own key offers the built-in one, a rejected built-in key only a retry`() {
        assertEquals(ErrorAction.UseBuiltInKey, AppError.Unauthorized.toErrorMessage(usingOwnKey = true).action)
        assertEquals(ErrorAction.TryAgain, AppError.Unauthorized.toErrorMessage(usingOwnKey = false).action)
    }

    @Test
    fun `other failures always offer try again`() {
        listOf(AppError.Offline, AppError.Timeout, AppError.Server).forEach { error ->
            assertEquals(ErrorAction.TryAgain, error.toErrorMessage(usingOwnKey = true).action)
        }
    }
}

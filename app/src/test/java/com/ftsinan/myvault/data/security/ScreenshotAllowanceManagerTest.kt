package com.ftsinan.myvault.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotAllowanceManagerTest {

    @Test
    fun allowanceExpiresAfterDuration() {
        var now = 1_000L
        val manager = ScreenshotAllowanceManager { now }

        manager.allowTemporarily(5_000L)

        assertEquals(6_000L, manager.expiresAtMillis.value)
        assertTrue(manager.isActive(5_999L))
        assertFalse(manager.isActive(6_000L))
    }

    @Test
    fun clearEndsAllowanceImmediately() {
        val manager = ScreenshotAllowanceManager { 1_000L }
        manager.allowTemporarily(5_000L)

        manager.clear()

        assertEquals(0L, manager.expiresAtMillis.value)
        assertFalse(manager.isActive(1_000L))
    }

    @Test
    fun nonPositiveDurationIsRejected() {
        val manager = ScreenshotAllowanceManager { 1_000L }

        assertThrows(IllegalArgumentException::class.java) {
            manager.allowTemporarily(0L)
        }
    }
}

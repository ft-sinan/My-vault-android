package com.ftsinan.myvault.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `aadhaar number is masked keeping last 4 digits`() {
        assertEquals("XXXX XXXX 9012", FormatUtils.maskDocumentNumber("1234 5678 9012"))
    }

    @Test
    fun `pan number is masked keeping last 4 characters`() {
        assertEquals("•••• 123F", FormatUtils.maskDocumentNumber("ABCDE123F"))
    }

    @Test
    fun `short numbers and nulls are handled`() {
        assertEquals("—", FormatUtils.maskDocumentNumber(null))
        assertEquals("—", FormatUtils.maskDocumentNumber(""))
        assertEquals("123", FormatUtils.maskDocumentNumber("123"))
    }

    @Test
    fun `expiry status detects expired documents`() {
        val now = 1_700_000_000_000L
        val yesterday = now - 2 * 24 * 60 * 60 * 1000L
        val status = FormatUtils.expiryStatus(yesterday, now)
        assertTrue(status is ExpiryStatus.Expired)
    }

    @Test
    fun `expiry status detects expiring soon`() {
        val now = 1_700_000_000_000L
        val inTenDays = now + 10 * 24 * 60 * 60 * 1000L
        val status = FormatUtils.expiryStatus(inTenDays, now)
        assertTrue(status is ExpiryStatus.ExpiringSoon)
    }

    @Test
    fun `expiry status is none without a date`() {
        assertEquals(ExpiryStatus.None, FormatUtils.expiryStatus(null))
    }
}

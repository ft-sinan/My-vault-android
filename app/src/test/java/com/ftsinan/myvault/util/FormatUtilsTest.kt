package com.ftsinan.myvault.util

import org.junit.Assert.assertEquals
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
}

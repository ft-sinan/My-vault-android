package com.ftsinan.myvault.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdNumberParserTest {

    @Test
    fun `detects and formats a Verhoeff valid Aadhaar number`() {
        val result = IdNumberParser.parse("Government of India 2345 6789 0124")

        assertEquals(DetectedId("Aadhaar Card", "2345 6789 0124"), result)
        assertTrue(IdNumberParser.isValidVerhoeff("234567890124"))
        assertFalse(IdNumberParser.isValidVerhoeff("234567890123"))
    }

    @Test
    fun `uses an Aadhaar candidate when the card type is explicit`() {
        val result = IdNumberParser.parse("AADHAAR 2345 6789 0123")

        assertEquals(DetectedId("Aadhaar Card", "2345 6789 0123"), result)
    }

    @Test
    fun `detects a PAN number`() {
        val result = IdNumberParser.parse("PERMANENT ACCOUNT NUMBER ABCDE1234F")

        assertEquals(DetectedId("PAN Card", "ABCDE1234F"), result)
    }

    @Test
    fun `detects and normalizes a driving licence number`() {
        val result = IdNumberParser.parse("DRIVING LICENCE KL 07 2011 0012345")

        assertEquals(DetectedId("Driving Licence", "KL0720110012345"), result)
    }

    @Test
    fun `can suggest a type without a trustworthy number`() {
        val result = IdNumberParser.parse("My Aadhaar, My Identity")

        assertEquals(DetectedId("Aadhaar Card", ""), result)
    }

    @Test
    fun `returns null for unrelated text`() {
        assertNull(IdNumberParser.parse("School leaving certificate"))
    }
}

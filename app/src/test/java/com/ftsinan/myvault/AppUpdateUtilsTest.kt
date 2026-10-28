package com.ftsinan.myvault

import com.ftsinan.myvault.util.ChecksumParser
import com.ftsinan.myvault.util.UpdateVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateUtilsTest {

    @Test
    fun `newer release versions are detected`() {
        assertTrue(UpdateVersion.isNewer("v1.1.0", "1.0.0"))
        assertTrue(UpdateVersion.isNewer("1.0.1", "1.0"))
        assertTrue(UpdateVersion.isNewer("2.0.0-beta", "1.9.9"))
    }

    @Test
    fun `same and older versions are not updates`() {
        assertFalse(UpdateVersion.isNewer("v1.1.0", "1.1.0"))
        assertFalse(UpdateVersion.isNewer("1.0.9", "1.1.0"))
        assertFalse(UpdateVersion.isNewer("1.1", "1.1.0"))
    }

    @Test
    fun `checksum parser selects the APK line`() {
        val text = """
            1111111111111111111111111111111111111111111111111111111111111111  other.apk
            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA  FTVault-v1.3.0.apk
        """.trimIndent()
        assertEquals(
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            ChecksumParser.expectedHash(text, "FTVault-v1.3.0.apk")
        )
    }

    @Test
    fun `checksum parser accepts a bare hash`() {
        val hash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        assertEquals(hash, ChecksumParser.expectedHash(hash, "FTVault-v1.3.0.apk"))
    }
}

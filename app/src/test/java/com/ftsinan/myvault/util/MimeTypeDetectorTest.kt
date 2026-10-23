package com.ftsinan.myvault.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MimeTypeDetectorTest {

    @Test
    fun `detects common document and image headers`() {
        assertEquals(
            "image/jpeg",
            MimeTypeDetector.detect(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()))
        )
        assertEquals(
            "image/png",
            MimeTypeDetector.detect(
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
            )
        )
        assertEquals(
            "application/pdf",
            MimeTypeDetector.detect("%PDF-1.7".toByteArray(Charsets.US_ASCII))
        )
        assertEquals(
            "image/webp",
            MimeTypeDetector.detect("RIFFxxxxWEBP".toByteArray(Charsets.US_ASCII))
        )
        assertEquals(
            "image/gif",
            MimeTypeDetector.detect("GIF89a".toByteArray(Charsets.US_ASCII))
        )
    }

    @Test
    fun `detects ISO image containers from ftyp brand`() {
        assertEquals(
            "image/heic",
            MimeTypeDetector.detect("xxxxftypheic".toByteArray(Charsets.US_ASCII))
        )
        assertEquals(
            "image/avif",
            MimeTypeDetector.detect("xxxxftypavif".toByteArray(Charsets.US_ASCII))
        )
    }

    @Test
    fun `unknown or incomplete headers return null`() {
        assertNull(MimeTypeDetector.detect(byteArrayOf()))
        assertNull(MimeTypeDetector.detect(byteArrayOf(0xFF.toByte(), 0xD8.toByte())))
        assertNull(MimeTypeDetector.detect("not-a-document".toByteArray(Charsets.US_ASCII)))
    }

    @Test
    fun `adds extension only when the provider did not supply one`() {
        assertEquals(
            "document_123.jpg",
            MimeTypeDetector.fileNameWithExtension("document_123", "image/jpeg")
        )
        assertEquals(
            "licence.pdf",
            MimeTypeDetector.fileNameWithExtension("licence.pdf", "application/pdf")
        )
    }
}

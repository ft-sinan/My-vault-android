package com.ftsinan.myvault.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ShareNamingTest {

    @Test
    fun `pdf file name puts owner first for personal documents`() {
        assertEquals(
            "Sinan mk - Driving Licence.pdf",
            ShareNaming.pdfFileName(ownerName = "Sinan mk", title = "Driving Licence")
        )
    }

    @Test
    fun `pdf file name is just the title for group documents`() {
        assertEquals(
            "Vehicle RC.pdf",
            ShareNaming.pdfFileName(ownerName = null, title = "Vehicle RC")
        )
        assertEquals(
            "Vehicle RC.pdf",
            ShareNaming.pdfFileName(ownerName = "  ", title = "Vehicle RC")
        )
    }

    @Test
    fun `pdf file name removes unsafe characters and duplicate extension`() {
        assertEquals(
            "A B - Licence Copy.pdf",
            ShareNaming.pdfFileName(ownerName = "A/B", title = "Licence: Copy?")
        )
        assertEquals(
            "Licence.pdf",
            ShareNaming.pdfFileName(ownerName = null, title = "Licence.pdf")
        )
        assertEquals(
            "document.pdf",
            ShareNaming.pdfFileName(ownerName = null, title = "   ")
        )
    }

    @Test
    fun `image share text has owner and type then the id number`() {
        assertEquals(
            "Sinan mk Aadhaar\n\n8983 7738 2277",
            ShareNaming.imageShareText(
                ownerName = "Sinan mk",
                docType = "Aadhaar",
                documentNumber = "8983 7738 2277",
                fallbackTitle = "My Aadhaar Card"
            )
        )
    }

    @Test
    fun `image share text omits the number line when no number is stored`() {
        assertEquals(
            "Sinan mk PAN Card",
            ShareNaming.imageShareText(
                ownerName = "Sinan mk",
                docType = "PAN Card",
                documentNumber = null,
                fallbackTitle = "My PAN"
            )
        )
        assertEquals(
            "Sinan mk PAN Card",
            ShareNaming.imageShareText(
                ownerName = "Sinan mk",
                docType = "PAN Card",
                documentNumber = "   ",
                fallbackTitle = "My PAN"
            )
        )
    }

    @Test
    fun `image share text falls back to title and works without an owner`() {
        assertEquals(
            "House Agreement\n\nREG-42",
            ShareNaming.imageShareText(
                ownerName = null,
                docType = "",
                documentNumber = "REG-42",
                fallbackTitle = "House Agreement"
            )
        )
    }
}

package com.ftsinan.myvault.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    fun formatDate(epochMillis: Long?): String =
        epochMillis?.let { dateFormat.format(Date(it)) } ?: "—"

    fun formatFileSize(bytes: Long): String = when {
        bytes <= 0 -> "Unknown size"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale.ENGLISH, "%.1f MB", bytes / (1024.0 * 1024.0))
    }

    /**
     * Masks a document number so curious onlookers only see the last 4
     * characters, e.g. Aadhaar 1234 5678 9012 -> XXXX XXXX 9012.
     */
    fun maskDocumentNumber(number: String?): String {
        if (number.isNullOrBlank()) return "—"
        val clean = number.trim()
        if (clean.length <= 4) return clean
        val last4 = clean.takeLast(4)
        val digitsOnly = clean.filter { it.isDigit() }
        return if (digitsOnly.length == 12 && digitsOnly.length == clean.replace(" ", "").length) {
            "XXXX XXXX $last4"
        } else {
            "•••• $last4"
        }
    }
}

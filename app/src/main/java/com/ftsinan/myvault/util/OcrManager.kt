package com.ftsinan.myvault.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class DetectedId(
    val docType: String,
    val number: String
)

/**
 * Extracts a likely ID number (and document type) from OCR text.
 * Kept free of Android/ML Kit types so it can be unit tested.
 * Results are only ever suggestions — the UI always lets the user
 * review and edit them before saving.
 */
object IdNumberParser {

    private val panRegex = Regex("""\b[A-Z]{5}[0-9]{4}[A-Z]\b""")
    private val aadhaarRegex = Regex("""\b\d{4}[\s-]?\d{4}[\s-]?\d{4}\b""")
    private val drivingLicenceRegexes = listOf(
        Regex("""\b[A-Z]{2}[- ]?\d{2}[- ]?(?:19|20)\d{2}[- ]?\d{7}\b"""),
        Regex("""\b[A-Z]{2}\d{13}\b""")
    )

    fun parse(rawText: String): DetectedId? {
        val text = rawText.uppercase()

        // Aadhaar: prefer a Verhoeff-valid 12 digit number.
        val aadhaarCandidates = aadhaarRegex.findAll(text)
            .map { it.value.filter(Char::isDigit) }
            .filter { it.length == 12 && it.first() !in listOf('0', '1') }
            .toList()
        val validAadhaar = aadhaarCandidates.firstOrNull { isValidVerhoeff(it) }
        if (validAadhaar != null) {
            return DetectedId("Aadhaar Card", formatAadhaar(validAadhaar))
        }
        if (aadhaarCandidates.isNotEmpty() &&
            ("AADHAAR" in text || "UIDAI" in text || "UNIQUE IDENTIFICATION" in text)
        ) {
            return DetectedId("Aadhaar Card", formatAadhaar(aadhaarCandidates.first()))
        }

        panRegex.find(text)?.let {
            return DetectedId("PAN Card", it.value)
        }

        val looksLikeLicence = "DRIVING" in text && ("LICENCE" in text || "LICENSE" in text)
        for (regex in drivingLicenceRegexes) {
            regex.find(text)?.let { match ->
                if (looksLikeLicence || regex == drivingLicenceRegexes[0]) {
                    return DetectedId(
                        "Driving Licence",
                        match.value.replace(" ", "").replace("-", "")
                    )
                }
            }
        }

        // Type-only hints without a trustworthy number.
        return when {
            "AADHAAR" in text || "UIDAI" in text -> DetectedId("Aadhaar Card", "")
            "PERMANENT ACCOUNT NUMBER" in text || "INCOME TAX" in text ->
                DetectedId("PAN Card", "")
            looksLikeLicence -> DetectedId("Driving Licence", "")
            else -> null
        }?.takeIf { it.number.isNotBlank() || true }
    }

    private fun formatAadhaar(digits: String): String =
        digits.chunked(4).joinToString(" ")

    // Verhoeff checksum used by Aadhaar numbers.
    private val verhoeffD = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
        intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
        intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
        intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
        intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
        intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
        intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
        intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
        intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
    )
    private val verhoeffP = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
        intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
        intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
        intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
        intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
        intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
        intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
    )

    internal fun isValidVerhoeff(digits: String): Boolean {
        var checksum = 0
        digits.reversed().forEachIndexed { index, char ->
            val digit = char.digitToIntOrNull() ?: return false
            checksum = verhoeffD[checksum][verhoeffP[index % 8][digit]]
        }
        return checksum == 0
    }
}

/**
 * Runs ML Kit's bundled Latin text recognizer entirely on-device over
 * picked/scanned image or PDF URIs and returns a detected ID suggestion.
 */
class OcrManager(context: Context) {

    private val appContext = context.applicationContext

    suspend fun detectId(uris: List<Uri>): DetectedId? = withContext(Dispatchers.IO) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val combinedText = StringBuilder()

            suspend fun inspect(image: InputImage): DetectedId? {
                val text = suspendCancellableCoroutine<String?> { continuation ->
                    recognizer.process(image)
                        .addOnSuccessListener { result ->
                            if (continuation.isActive) continuation.resume(result.text)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
                if (!text.isNullOrBlank()) {
                    combinedText.appendLine(text)
                    return IdNumberParser.parse(combinedText.toString())
                        ?.takeIf { it.number.isNotBlank() }
                }
                return null
            }

            for (uri in uris.take(2)) {
                val mimeType = appContext.contentResolver.getType(uri).orEmpty()
                if (mimeType.equals("application/pdf", ignoreCase = true)) {
                    val pdfResult = runCatching {
                        appContext.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                            PdfRenderer(descriptor).use { renderer ->
                                // OCR the first two pages; ID numbers are usually on page one.
                                for (pageIndex in 0 until minOf(renderer.pageCount, 2)) {
                                    val page = renderer.openPage(pageIndex)
                                    val bitmap = try {
                                        val scale = minOf(
                                            2f,
                                            1600f / page.width.coerceAtLeast(1),
                                            2000f / page.height.coerceAtLeast(1)
                                        )
                                        Bitmap.createBitmap(
                                            (page.width * scale).toInt().coerceAtLeast(1),
                                            (page.height * scale).toInt().coerceAtLeast(1),
                                            Bitmap.Config.ARGB_8888
                                        ).also { output ->
                                            output.eraseColor(android.graphics.Color.WHITE)
                                            page.render(
                                                output,
                                                null,
                                                null,
                                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                            )
                                        }
                                    } finally {
                                        page.close()
                                    }
                                    val detected = try {
                                        inspect(InputImage.fromBitmap(bitmap, 0))
                                    } finally {
                                        bitmap.recycle()
                                    }
                                    if (detected != null) return@use detected
                                }
                                null
                            }
                        }
                    }.getOrNull()
                    if (pdfResult != null) return@withContext pdfResult
                } else {
                    val imageResult = runCatching {
                        inspect(InputImage.fromFilePath(appContext, uri))
                    }.getOrNull()
                    if (imageResult != null) return@withContext imageResult
                }
            }
            IdNumberParser.parse(combinedText.toString())
        } catch (t: Throwable) {
            null
        } finally {
            runCatching { recognizer.close() }
        }
    }
}

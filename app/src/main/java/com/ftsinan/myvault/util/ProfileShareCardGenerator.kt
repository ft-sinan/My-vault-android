package com.ftsinan.myvault.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.storage.VaultFileManager
import java.io.File
import java.io.FileOutputStream

/**
 * Renders a clean, shareable profile ID card as a PNG image.
 * Includes every available profile detail except relation, and carries
 * no app branding. The image is written only to the share cache, which
 * is wiped when the vault locks.
 */
data class ProfileShareLabels(
    val dateOfBirth: String = "Date of Birth",
    val gender: String = "Gender",
    val bloodGroup: String = "Blood Group",
    val genderValues: Map<String, String> = emptyMap(),
    val unknownBloodGroup: String = "Unknown"
)

class ProfileShareCardGenerator(
    private val fileManager: VaultFileManager
) {

    fun create(
        member: MemberProfile,
        photoBytes: ByteArray?,
        labels: ProfileShareLabels = ProfileShareLabels()
    ): File {
        val width = 1200
        val height = 1500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(244, 248, 248) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), background)

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(28f, 0f, 10f, Color.argb(45, 0, 0, 0))
        }
        val cardRect = RectF(70f, 80f, width - 70f, height - 80f)
        canvas.drawRoundRect(cardRect, 56f, 56f, cardPaint)

        // Header band
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(8, 76, 84) }
        val headerRect = RectF(70f, 80f, width - 70f, 430f)
        canvas.save()
        val headerPath = Path().apply {
            addRoundRect(cardRect, 56f, 56f, Path.Direction.CW)
        }
        canvas.clipPath(headerPath)
        canvas.drawRect(headerRect, headerPaint)
        canvas.restore()

        drawAvatar(canvas, member, photoBytes, centerX = width / 2f, centerY = 255f, radius = 128f)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 29, 33)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = fittedTextSize(member.name, 66f, width - 220f, this)
        }
        canvas.drawText(member.name, width / 2f, 565f, namePaint)

        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 137, 143)
        }
        canvas.drawRoundRect(RectF(width / 2f - 70f, 600f, width / 2f + 70f, 610f), 5f, 5f, divider)

        val rows = buildList {
            member.dateOfBirth?.let { add(labels.dateOfBirth to FormatUtils.formatDate(it)) }
            member.gender?.takeIf { it.isNotBlank() }?.let {
                add(labels.gender to (labels.genderValues[it] ?: it))
            }
            member.bloodGroup?.takeIf { it.isNotBlank() }?.let {
                add(
                    labels.bloodGroup to
                        if (it == "Unknown") labels.unknownBloodGroup else it
                )
            }
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(102, 116, 120)
            textSize = 34f
            typeface = Typeface.DEFAULT
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(18, 29, 33)
            textSize = 43f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        rows.forEachIndexed { index, (label, value) ->
            val y = 735f + index * 170f
            canvas.drawText(label, 150f, y, labelPaint)
            canvas.drawText(value, 150f, y + 58f, valuePaint)
        }

        val safeName = member.name.replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "profile" }
        val outFile = fileManager.shareCacheFile("profile_${member.id}_$safeName.png")
        FileOutputStream(outFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        return outFile
    }

    private fun drawAvatar(
        canvas: Canvas,
        member: MemberProfile,
        photoBytes: ByteArray?,
        centerX: Float,
        centerY: Float,
        radius: Float
    ) {
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawCircle(centerX, centerY, radius + 8f, border)

        val photo = photoBytes?.let {
            runCatching { BitmapFactory.decodeByteArray(it, 0, it.size) }.getOrNull()
        }
        if (photo != null) {
            val cropped = centreCropSquare(photo, (radius * 2).toInt())
            canvas.save()
            val clip = Path().apply { addCircle(centerX, centerY, radius, Path.Direction.CW) }
            canvas.clipPath(clip)
            canvas.drawBitmap(
                cropped,
                centerX - radius,
                centerY - radius,
                Paint(Paint.ANTI_ALIAS_FLAG)
            )
            canvas.restore()
            if (cropped !== photo) cropped.recycle()
            photo.recycle()
        } else {
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(159, 240, 243)
            }
            canvas.drawCircle(centerX, centerY, radius, circlePaint)
            val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0, 45, 50)
                textAlign = Paint.Align.CENTER
                textSize = 92f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val initials = member.name.trim().split(Regex("\\s+"))
                .filter { it.isNotBlank() }.take(2)
                .joinToString("") { it.take(1).uppercase() }
                .ifBlank { "?" }
            val fontMetrics = initialsPaint.fontMetrics
            val baseline = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
            canvas.drawText(initials, centerX, baseline, initialsPaint)
        }
    }

    private fun centreCropSquare(source: Bitmap, targetSize: Int): Bitmap {
        val side = minOf(source.width, source.height)
        val x = (source.width - side) / 2
        val y = (source.height - side) / 2
        val square = Bitmap.createBitmap(source, x, y, side, side)
        if (square.width == targetSize) return square
        return Bitmap.createScaledBitmap(square, targetSize, targetSize, true).also {
            if (it !== square) square.recycle()
        }
    }

    private fun fittedTextSize(text: String, startSize: Float, maxWidth: Float, paint: Paint): Float {
        var size = startSize
        paint.textSize = size
        while (size > 34f && paint.measureText(text) > maxWidth) {
            size -= 4f
            paint.textSize = size
        }
        return size
    }
}

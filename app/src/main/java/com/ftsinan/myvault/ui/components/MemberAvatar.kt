package com.ftsinan.myvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** Round profile avatar: decrypted photo when present, initials otherwise. */
@Composable
fun MemberAvatar(
    name: String,
    photoBytes: ByteArray?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    seedColor: Color? = null
) {
    val containerColor = seedColor?.let { expressivePastel(it) }
        ?: MaterialTheme.colorScheme.primaryContainer
    val initialsColor = if (seedColor != null) {
        readableContentColor(containerColor)
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .then(
                if (seedColor != null) {
                    Modifier.border(2.dp, seedColor.copy(alpha = 0.62f), CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoBytes != null) {
            AsyncImage(
                model = photoBytes,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Text(
                text = initials(name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = initialsColor
            )
        }
    }
}

fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "?"
    return parts.take(2).joinToString("") { it.take(1).uppercase() }
}

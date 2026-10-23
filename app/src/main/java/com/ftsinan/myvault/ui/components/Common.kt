package com.ftsinan.myvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.DocumentSummary
import com.ftsinan.myvault.util.FormatUtils

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MorphingShapeBox(
            modifier = Modifier.size(96.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            lobes = 9,
            wave = 0.15f,
            spinMillis = 22_000
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/**
 * Document list card. Long titles stay on one line, but expose a small
 * corner expand/collapse button that wraps the *title only* (same font
 * size) so the full name can be read without opening the document.
 */
@Composable
fun DocumentCard(
    summary: DocumentSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val document = summary.document
    var titleExpanded by remember(document.id) { mutableStateOf(false) }
    var titleOverflows by remember(document.id, document.title) { mutableStateOf(false) }

    val cardShape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .springPress(cardShape, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            MorphingShapeBox(
                modifier = Modifier.size(58.dp),
                color = categoryColor(document.category).copy(alpha = 0.16f),
                lobes = 9,
                wave = 0.13f,
                spinMillis = 24_000
            ) {
                Icon(
                    imageVector = categoryIcon(document.category),
                    contentDescription = categoryLabel(document.category),
                    tint = categoryColor(document.category),
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = if (titleExpanded) Int.MAX_VALUE else 1,
                        overflow = if (titleExpanded) TextOverflow.Visible
                        else TextOverflow.Ellipsis,
                        onTextLayout = { layout ->
                            if (!titleExpanded) {
                                titleOverflows = layout.hasVisualOverflow
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (document.isFavourite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFF2B544),
                            modifier = Modifier
                                .padding(start = 4.dp, top = 2.dp)
                                .size(16.dp)
                        )
                    }
                    if (titleExpanded || titleOverflows) {
                        IconButton(
                            onClick = { titleExpanded = !titleExpanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (titleExpanded) Icons.Filled.ExpandLess
                                else Icons.Filled.ExpandMore,
                                contentDescription = document.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                val assignedLabel = document.assignedGroup?.let { assignedGroupLabel(it) }
                    ?: summary.assignedName
                Text(
                    text = "${document.docType} • $assignedLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (!document.documentNumber.isNullOrBlank()) {
                        Text(
                            text = FormatUtils.maskDocumentNumber(document.documentNumber),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (summary.pageCount > 1) {
                        SmallInfoChip(
                            text = stringResource(R.string.pages_count, summary.pageCount)
                        )
                    }
                    if (summary.isPdf) {
                        SmallInfoChip(text = "PDF")
                    }
                }
            }

            Icon(
                imageVector = if (summary.isPdf) Icons.Filled.PictureAsPdf
                else Icons.Filled.Description,
                contentDescription = if (summary.isPdf) "PDF" else null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
        }
    }
}

@Composable
private fun SmallInfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

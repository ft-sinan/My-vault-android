package com.ftsinan.myvault.ui.screens.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.ui.components.EmptyState
import com.ftsinan.myvault.ui.components.ExpressiveStat
import com.ftsinan.myvault.ui.components.HeaderCircleButton
import com.ftsinan.myvault.ui.components.MorphingShapeBox
import com.ftsinan.myvault.ui.components.categoryColor
import com.ftsinan.myvault.ui.components.categoryIcon
import com.ftsinan.myvault.ui.components.expressiveReveal
import com.ftsinan.myvault.ui.components.categoryLabel
import com.ftsinan.myvault.ui.components.assignedGroupIcon
import com.ftsinan.myvault.ui.components.assignedGroupLabel
import com.ftsinan.myvault.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: DocumentDetailViewModel = viewModel(
        factory = DocumentDetailViewModel.factory(documentId)
    )
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val document by viewModel.document.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val previews by viewModel.previews.collectAsState()
    val previewError by viewModel.previewError.collectAsState()
    val visualPageCount by viewModel.visualPageCount.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()

    var showNumber by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var preparingShare by remember { mutableStateOf(false) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) onBack()
    }
    fun launchShare(payload: SharePayload) {
        val intent = if (payload.uris.size > 1) {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = payload.mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(payload.uris))
                putExtra(Intent.EXTRA_SUBJECT, payload.title)
                payload.text?.takeIf { it.isNotBlank() }?.let {
                    putExtra(Intent.EXTRA_TEXT, it)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = payload.mimeType
                putExtra(Intent.EXTRA_STREAM, payload.uris.firstOrNull())
                putExtra(Intent.EXTRA_SUBJECT, payload.title)
                payload.text?.takeIf { it.isNotBlank() }?.let {
                    putExtra(Intent.EXTRA_TEXT, it)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { if (!preparingShare) showShareDialog = false },
            title = { Text(stringResource(R.string.share_choose_format)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            preparingShare = true
                            scope.launch {
                                val payload = viewModel.prepareShare(ShareFormat.PDF)
                                preparingShare = false
                                showShareDialog = false
                                if (payload != null) launchShare(payload)
                                else Toast.makeText(
                                    context,
                                    context.getString(R.string.share_failed),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        enabled = !preparingShare,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.share_as_pdf))
                    }
                    OutlinedButton(
                        onClick = {
                            preparingShare = true
                            scope.launch {
                                val payload = viewModel.prepareShare(ShareFormat.IMAGES)
                                preparingShare = false
                                showShareDialog = false
                                if (payload != null) launchShare(payload)
                                else Toast.makeText(
                                    context,
                                    context.getString(R.string.share_failed),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        enabled = !preparingShare,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.share_as_images))
                    }
                    if (preparingShare) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(Modifier.size(18.dp))
                            Text(stringResource(R.string.share_preparing))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showShareDialog = false },
                    enabled = !preparingShare
                ) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val doc = document
        if (doc == null) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                val pageCount = visualPageCount.takeIf { it > 0 }
                    ?: pages.size.takeIf { it > 0 }
                    ?: 1
                val heroSize = pages.sumOf { it.fileSize }.takeIf { it > 0 } ?: doc.fileSize
                DocumentDetailHero(
                    document = doc,
                    pageCount = pageCount,
                    totalSize = heroSize,
                    onBack = onBack,
                    onToggleFavourite = viewModel::toggleFavourite,
                    onShare = { showShareDialog = true },
                    onEdit = { onEdit(doc.id) },
                    onDelete = { showDeleteDialog = true },
                    modifier = Modifier.expressiveReveal(0, doc.id)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow(
                            icon = doc.assignedGroup?.let { assignedGroupIcon(it) }
                                ?: Icons.Filled.Person,
                            label = stringResource(R.string.detail_belongs_to),
                            value = doc.assignedGroup?.let { assignedGroupLabel(it) }
                                ?: doc.holderName
                        )
                        DetailRow(
                            icon = Icons.Filled.Description,
                            label = stringResource(R.string.detail_pages),
                            value = visualPageCount.takeIf { it > 0 }?.toString()
                                ?: pages.size.takeIf { it > 0 }?.toString()
                                ?: "1"
                        )
                        val totalSize = pages.sumOf { it.fileSize }
                            .takeIf { it > 0 } ?: doc.fileSize
                        DetailRow(
                            icon = Icons.Filled.InsertDriveFile,
                            label = stringResource(R.string.detail_file_size),
                            value = FormatUtils.formatFileSize(totalSize)
                        )
                        DetailRow(
                            icon = Icons.Filled.CalendarMonth,
                            label = stringResource(R.string.detail_added_on),
                            value = DateFormat.getMediumDateFormat(context)
                                .format(Date(doc.createdAt))
                        )
                        doc.documentNumber?.takeIf { it.isNotBlank() }?.let { number ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.detail_document_number),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        if (showNumber) number else FormatUtils.maskDocumentNumber(number),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { showNumber = !showNumber }) {
                                    Icon(
                                        if (showNumber) Icons.Filled.VisibilityOff
                                        else Icons.Filled.Visibility,
                                        contentDescription = null
                                    )
                                }
                                IconButton(onClick = {
                                    clipboard.setText(AnnotatedString(number))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.action_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                                }
                            }
                        }
                        doc.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                            Column {
                                Text(
                                    stringResource(R.string.detail_notes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(notes)
                            }
                        }
                    }
                }
            }

            when {
                previews == null -> item {
                    Box(
                        Modifier.fillMaxWidth().height(220.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                previewError != null || previews.isNullOrEmpty() -> item {
                    EmptyState(
                        icon = Icons.Filled.InsertDriveFile,
                        title = stringResource(R.string.detail_preview_unavailable),
                        subtitle = stringResource(R.string.detail_preview_unavailable_message)
                    )
                }
                else -> {
                    val safePreviews = previews.orEmpty()
                    var pageOffset = 0
                    safePreviews.forEach { preview ->
                        when (preview) {
                            is PagePreview.Image -> {
                                val pageNumber = pageOffset + 1
                                item {
                                    PageLabel(pageNumber, visualPageCount.coerceAtLeast(safePreviews.size))
                                    ZoomableImage(bytes = preview.bytes)
                                }
                                pageOffset += 1
                            }
                            is PagePreview.Pdf -> {
                                item {
                                    PdfPageList(file = preview.file)
                                }
                                // Imported PDFs are stored as one file; detailed
                                // internal labels are handled inside PdfPageList.
                                pageOffset = visualPageCount
                            }
                            is PagePreview.Other -> {
                                item {
                                    EmptyState(
                                        icon = Icons.Filled.InsertDriveFile,
                                        title = stringResource(R.string.detail_preview_unavailable),
                                        subtitle = stringResource(R.string.detail_preview_unavailable_message)
                                    )
                                }
                                pageOffset += 1
                            }
                        }
                    }
                }
            }
        }
    }
}


/** App-detail style hero: floating actions, expressive icon, title and stat strip. */
@Composable
private fun DocumentDetailHero(
    document: VaultDocument,
    pageCount: Int,
    totalSize: Long,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryTint = categoryColor(document.category)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        categoryTint.copy(alpha = 0.24f),
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
            .padding(16.dp)
    ) {
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-66).dp)
                .size(170.dp),
            color = categoryTint.copy(alpha = 0.13f),
            lobes = 12,
            wave = 0.19f,
            spinMillis = 28_000
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
                Spacer(Modifier.weight(1f))
                HeaderCircleButton(
                    icon = if (document.isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(R.string.home_favourites),
                    onClick = onToggleFavourite,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
                HeaderCircleButton(
                    icon = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.action_share),
                    onClick = onShare,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
                HeaderCircleButton(
                    icon = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.action_edit),
                    onClick = onEdit,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
                HeaderCircleButton(
                    icon = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    onClick = onDelete,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Top) {
                MorphingShapeBox(
                    modifier = Modifier.size(84.dp),
                    color = categoryTint.copy(alpha = 0.18f),
                    lobes = 9,
                    wave = 0.14f,
                    spinMillis = 20_000
                ) {
                    Icon(
                        imageVector = categoryIcon(document.category),
                        contentDescription = categoryLabel(document.category),
                        tint = categoryTint,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = document.docType,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = document.assignedGroup?.let { assignedGroupLabel(it) }
                            ?: document.holderName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.62f))
                    .padding(horizontal = 10.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpressiveStat(
                    value = pageCount.toString(),
                    label = stringResource(R.string.detail_pages),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ExpressiveStat(
                    value = FormatUtils.formatFileSize(totalSize),
                    label = stringResource(R.string.detail_file_size),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ExpressiveStat(
                    value = categoryLabel(document.category),
                    label = stringResource(R.string.docs_categories),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MorphingShapeBox(
            modifier = Modifier.size(38.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            lobes = 9,
            wave = 0.13f,
            spinMillis = 24_000
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PageLabel(pageNumber: Int, totalPages: Int) {
    Text(
        stringResource(R.string.detail_page_of, pageNumber, totalPages.coerceAtLeast(pageNumber)),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ZoomableImage(bytes: ByteArray) {
    var scale by remember(bytes.size) { mutableStateOf(1f) }
    var offsetX by remember(bytes.size) { mutableStateOf(0f) }
    var offsetY by remember(bytes.size) { mutableStateOf(0f) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(MaterialTheme.shapes.large)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
        ) {
            AsyncImage(
                model = bytes,
                contentDescription = stringResource(R.string.detail_document_preview),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
            )
        }
    }
}

@Composable
private fun PdfPageList(file: File) {
    val bitmaps by produceState<List<Bitmap>?>(initialValue = null, file.absolutePath) {
        value = withContext(Dispatchers.IO) {
            runCatching { renderPdfPages(file) }.getOrDefault(emptyList())
        }
    }
    when {
        bitmaps == null -> Box(
            Modifier.fillMaxWidth().height(220.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        bitmaps.isNullOrEmpty() -> EmptyState(
            icon = Icons.Filled.PictureAsPdf,
            title = stringResource(R.string.detail_preview_unavailable),
            subtitle = stringResource(R.string.detail_preview_unavailable_message)
        )
        else -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            bitmaps!!.forEachIndexed { index, bitmap ->
                Column {
                    PageLabel(index + 1, bitmaps!!.size)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(
                                R.string.detail_page_of,
                                index + 1,
                                bitmaps!!.size
                            ),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth().background(
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun renderPdfPages(file: File): List<Bitmap> {
    val output = mutableListOf<Bitmap>()
    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    descriptor.use { fd ->
        PdfRenderer(fd).use { renderer ->
            for (index in 0 until renderer.pageCount) {
                renderer.openPage(index).use { page ->
                    val width = (page.width * 2).coerceAtLeast(1)
                    val height = (page.height * 2).coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    output += bitmap
                }
            }
        }
    }
    return output
}

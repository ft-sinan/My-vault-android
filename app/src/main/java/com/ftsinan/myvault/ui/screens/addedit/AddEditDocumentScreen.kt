package com.ftsinan.myvault.ui.screens.addedit

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.DocumentTypes
import com.ftsinan.myvault.ui.components.ExpressiveBackTopBar
import com.ftsinan.myvault.ui.components.assignedGroupLabel
import com.ftsinan.myvault.ui.components.categoryLabel
import com.ftsinan.myvault.ui.screens.lock.findFragmentActivity
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditDocumentScreen(
    documentId: Long?,
    initialSource: ImportSource = ImportSource.NONE,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditDocumentViewModel = viewModel(factory = AddEditDocumentViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scanLabel = stringResource(R.string.add_scan)
    val fileLabel = stringResource(R.string.add_file)
    val scannerFailedText = stringResource(R.string.add_scanner_failed)

    LaunchedEffect(documentId) { viewModel.load(documentId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    val scanner = remember {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(10)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult
                .fromActivityResultIntent(result.data)
            val uris = scanResult?.pages?.map { it.imageUri }.orEmpty()
            if (uris.isNotEmpty()) viewModel.onPagesPicked(uris, scanLabel)
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onPagesPicked(listOf(uri), fileLabel)
    }

    fun launchScanner() {
        val activity = context.findFragmentActivity()
        if (activity == null) {
            viewModel.onScannerError(scannerFailedText)
            return
        }
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                runCatching {
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }.onFailure { viewModel.onScannerError(scannerFailedText) }
            }
            .addOnFailureListener { viewModel.onScannerError(scannerFailedText) }
    }

    fun launchFilePicker() {
        fileLauncher.launch(arrayOf("application/pdf", "image/*"))
    }

    var autoLaunchDone by rememberSaveable(documentId, initialSource) {
        mutableStateOf(false)
    }
    LaunchedEffect(initialSource, documentId) {
        if (!autoLaunchDone && documentId == null) {
            autoLaunchDone = true
            when (initialSource) {
                ImportSource.SCAN -> launchScanner()
                ImportSource.FILE -> launchFilePicker()
                ImportSource.NONE -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExpressiveBackTopBar(
                title = stringResource(
                    if (uiState.isEditMode) R.string.edit_title else R.string.add_title
                ),
                subtitle = stringResource(R.string.add_encrypted_note),
                icon = Icons.Filled.QrCodeScanner,
                backContentDescription = stringResource(R.string.action_back),
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_encrypting))
                    } else {
                        Text(
                            stringResource(
                                if (uiState.isEditMode) R.string.action_save_changes
                                else R.string.add_save_to_vault
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel(stringResource(R.string.add_document_file))
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (!uiState.isEditMode && uiState.pendingUris.isNotEmpty() &&
                        !uiState.pendingIsPdf
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.pendingUris) { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 104.dp, height = 138.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.pendingIsPdf) Icons.Filled.PictureAsPdf
                            else Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when {
                                uiState.isEditMode -> stringResource(
                                    R.string.pages_count,
                                    uiState.existingPages.size.coerceAtLeast(1)
                                )
                                uiState.pendingUris.size > 1 -> stringResource(
                                    R.string.pages_count,
                                    uiState.pendingUris.size
                                )
                                else -> uiState.pendingFileLabel
                                    ?: stringResource(R.string.add_no_file)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.add_pages_fixed_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!uiState.isEditMode) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { launchScanner() },
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, null)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.add_scan))
                            }
                            FilledTonalButton(
                                onClick = { launchFilePicker() },
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Icon(Icons.Filled.FolderOpen, null)
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.add_file))
                            }
                        }
                    }
                }
            }

            SectionLabel(stringResource(R.string.field_category))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.onCategoryChange(category) },
                        label = { Text(categoryLabel(category)) }
                    )
                }
            }

            SectionLabel(stringResource(R.string.field_doc_type))
            OutlinedTextField(
                value = uiState.docType,
                onValueChange = viewModel::onDocTypeChange,
                label = { Text(stringResource(R.string.field_doc_type_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DocumentTypes.suggestionsFor(uiState.category).forEach { suggestion ->
                    FilterChip(
                        selected = uiState.docType == suggestion,
                        onClick = {
                            viewModel.onDocTypeChange(suggestion)
                            if (uiState.title.isBlank()) viewModel.onTitleChange(suggestion)
                        },
                        label = { Text(suggestion) }
                    )
                }
            }

            SectionLabel(stringResource(R.string.field_title))
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.field_title_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel(stringResource(R.string.field_belongs_to))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.assignmentMode == AssignmentMode.PERSON,
                    onClick = { viewModel.onAssignmentModeChange(AssignmentMode.PERSON) },
                    label = { Text(stringResource(R.string.assignment_person)) }
                )
                FilterChip(
                    selected = uiState.assignmentMode == AssignmentMode.GROUP,
                    onClick = { viewModel.onAssignmentModeChange(AssignmentMode.GROUP) },
                    label = { Text(stringResource(R.string.assignment_group)) }
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.assignmentMode == AssignmentMode.PERSON) {
                    uiState.members.forEach { member ->
                        FilterChip(
                            selected = uiState.selectedMemberId == member.id,
                            onClick = { viewModel.onMemberSelected(member.id) },
                            label = { Text(member.name) }
                        )
                    }
                } else {
                    AssignedGroup.entries.forEach { group ->
                        FilterChip(
                            selected = uiState.selectedGroup == group,
                            onClick = { viewModel.onGroupSelected(group) },
                            label = { Text(assignedGroupLabel(group)) }
                        )
                    }
                }
            }

            SectionLabel(stringResource(R.string.field_document_number))
            OutlinedTextField(
                value = uiState.documentNumber,
                onValueChange = viewModel::onDocumentNumberChange,
                label = { Text(stringResource(R.string.field_document_number_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
            if (!uiState.pendingIsPdf && uiState.pendingUris.isNotEmpty()) {
                when {
                    uiState.isOcrRunning -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.ocr_looking),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    uiState.ocrCompleted && uiState.ocrFoundNumber -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.ocr_found),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    uiState.ocrCompleted -> {
                        Text(
                            stringResource(R.string.ocr_not_found),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = when (error) {
                        "ERR_DOC_TYPE_REQUIRED" -> stringResource(R.string.error_doc_type_required)
                        "ERR_TITLE_REQUIRED" -> stringResource(R.string.error_title_required)
                        "ERR_OWNER_REQUIRED" -> stringResource(R.string.error_owner_required)
                        "ERR_FILE_REQUIRED" -> stringResource(R.string.error_file_required)
                        "ERR_SAVE_FAILED" -> stringResource(R.string.error_save_failed)
                        else -> error
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

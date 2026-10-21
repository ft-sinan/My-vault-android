package com.ftsinan.myvault.ui.screens.addedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.DocumentTypes
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditDocumentScreen(
    documentId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditDocumentViewModel = viewModel(factory = AddEditDocumentViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) { viewModel.load(documentId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    // --- File pickers ---------------------------------------------------
    var captureUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) viewModel.onFilePicked(captureUri, label = "Captured photo")
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onFilePicked(uri, label = "Gallery image") }
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> viewModel.onFilePicked(uri, label = "Selected file") }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Column {
                        Text(
                            text = if (uiState.isEditMode) "Edit document" else "Add document",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Encrypted before it is saved",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encrypting & saving…")
                    } else {
                        Text(if (uiState.isEditMode) "Save changes" else "Save to vault")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- Attached file -------------------------------------------
            SectionLabel("Document file *")
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.pendingUri != null) {
                        AsyncImage(
                            model = uiState.pendingUri,
                            contentDescription = "Selected document preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.pendingFileLabel
                                ?: "No file attached yet",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = {
                                val uri = viewModel.createCaptureUri()
                                captureUri = uri
                                cameraLauncher.launch(uri)
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Scan")
                        }
                        FilledTonalButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Gallery")
                        }
                        FilledTonalButton(
                            onClick = {
                                fileLauncher.launch(arrayOf("application/pdf", "image/*"))
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("PDF/File")
                        }
                    }
                }
            }

            // --- Category --------------------------------------------------
            SectionLabel("Category")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.onCategoryChange(category) },
                        label = { Text(category.label) }
                    )
                }
            }

            // --- Document type --------------------------------------------
            SectionLabel("Document type *")
            OutlinedTextField(
                value = uiState.docType,
                onValueChange = viewModel::onDocTypeChange,
                label = { Text("e.g. Aadhaar Card") },
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

            // --- Title -----------------------------------------------------
            SectionLabel("Title *")
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("e.g. My Aadhaar Card") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // --- Holder ----------------------------------------------------
            SectionLabel("Whose document is this? *")
            OutlinedTextField(
                value = uiState.holderName,
                onValueChange = viewModel::onHolderNameChange,
                label = { Text("Full name on the document") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Relation.entries.forEach { relation ->
                    FilterChip(
                        selected = uiState.relation == relation,
                        onClick = { viewModel.onRelationChange(relation) },
                        label = { Text(relation.label) }
                    )
                }
            }

            // --- Number / dates / notes -------------------------------------
            SectionLabel("Document number (optional)")
            OutlinedTextField(
                value = uiState.documentNumber,
                onValueChange = viewModel::onDocumentNumberChange,
                label = { Text("Aadhaar / PAN / passport number…") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            DateField(
                label = "Issue date (optional)",
                value = uiState.issueDate,
                onChange = viewModel::onIssueDateChange
            )
            DateField(
                label = if (DocumentTypes.usuallyHasExpiry(uiState.docType)) {
                    "Expiry date (recommended)"
                } else {
                    "Expiry date (optional)"
                },
                value = uiState.expiryDate,
                onChange = viewModel::onExpiryDateChange
            )

            SectionLabel("Notes (optional)")
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Anything to remember about this document") },
                minLines = 2,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: Long?,
    onChange: (Long?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Column {
        SectionLabel(label)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = if (value == null) "" else FormatUtils.formatDate(value),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Tap to pick a date") },
            leadingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Pick date")
                }
            },
            trailingIcon = {
                if (value != null) {
                    IconButton(onClick = { onChange(null) }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear date")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Make the whole field tappable via a text button below for simplicity
        if (value == null) {
            TextButton(onClick = { showPicker = true }) { Text("Pick a date") }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(state.selectedDateMillis)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }
}

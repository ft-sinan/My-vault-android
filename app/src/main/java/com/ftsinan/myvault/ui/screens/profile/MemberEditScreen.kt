package com.ftsinan.myvault.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.ProfileOptions
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.ui.components.ExpressiveBackTopBar
import com.ftsinan.myvault.ui.components.MemberAvatar
import com.ftsinan.myvault.ui.components.bloodGroupLabel
import com.ftsinan.myvault.ui.components.genderLabel
import com.ftsinan.myvault.ui.components.relationLabel
import com.ftsinan.myvault.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemberEditScreen(
    memberId: Long?,
    ownerSetup: Boolean = false,
    onBack: (() -> Unit)? = null,
    onSaved: () -> Unit,
    viewModel: MemberEditViewModel = viewModel(
        factory = MemberEditViewModel.factory(memberId, ownerSetup)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(memberId, ownerSetup) { viewModel.load() }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onPhotoPicked(uri) }

    val titleRes = when {
        ownerSetup -> R.string.profile_setup_title
        uiState.existing == null -> R.string.member_add_title
        uiState.isOwner -> R.string.profile_edit_title
        else -> R.string.member_edit_title
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExpressiveBackTopBar(
                title = stringResource(titleRes),
                subtitle = if (ownerSetup) {
                    stringResource(R.string.profile_setup_subtitle)
                } else {
                    null
                },
                icon = Icons.Filled.Person,
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
                    }
                    Text(
                        stringResource(
                            if (uiState.existing == null) R.string.action_save
                            else R.string.action_save_changes
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.pendingPhotoUri != null) {
                    AsyncImage(
                        model = uiState.pendingPhotoUri,
                        contentDescription = stringResource(R.string.field_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                    )
                } else {
                    MemberAvatar(
                        name = uiState.name.ifBlank { "?" },
                        photoBytes = uiState.existingPhotoBytes,
                        size = 104.dp
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        photoLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(
                            if (uiState.pendingPhotoUri == null &&
                                uiState.existingPhotoBytes == null
                            ) R.string.photo_add else R.string.photo_change
                        )
                    )
                }
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.field_name)) },
                placeholder = { Text(stringResource(R.string.field_name_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (!uiState.isOwner) {
                FieldLabel(stringResource(R.string.field_relation))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Relation.entries
                        .filter { it != Relation.SELF }
                        .forEach { relation ->
                            FilterChip(
                                selected = uiState.relation == relation,
                                onClick = { viewModel.onRelationChange(relation) },
                                label = { Text(relationLabel(relation)) }
                            )
                        }
                }
            }

            ProfileDateField(
                label = stringResource(R.string.field_dob),
                value = uiState.dateOfBirth,
                onChange = viewModel::onDateOfBirthChange
            )

            FieldLabel(stringResource(R.string.field_gender))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileOptions.genders.forEach { gender ->
                    FilterChip(
                        selected = uiState.gender == gender,
                        onClick = {
                            viewModel.onGenderChange(
                                if (uiState.gender == gender) null else gender
                            )
                        },
                        label = { Text(genderLabel(gender)) }
                    )
                }
            }

            FieldLabel(stringResource(R.string.field_blood_group))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileOptions.bloodGroups.forEach { group ->
                    FilterChip(
                        selected = uiState.bloodGroup == group,
                        onClick = {
                            viewModel.onBloodGroupChange(
                                if (uiState.bloodGroup == group) null else group
                            )
                        },
                        label = { Text(bloodGroupLabel(group)) }
                    )
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = when (error) {
                        "NAME_REQUIRED" -> stringResource(R.string.profile_name_required)
                        "PROFILE_SAVE_FAILED" -> stringResource(R.string.error_profile_save_failed)
                        else -> error
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDateField(
    label: String,
    value: Long?,
    onChange: (Long?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label)
        OutlinedTextField(
            value = if (value == null) "" else FormatUtils.formatDate(value),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(stringResource(R.string.date_pick_hint)) },
            leadingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.DateRange, contentDescription = label)
                }
            },
            trailingIcon = {
                if (value != null) {
                    IconButton(onClick = { onChange(null) }) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.action_clear)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (value == null) {
            TextButton(onClick = { showPicker = true }) {
                Text(stringResource(R.string.date_pick_action))
            }
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
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }
}

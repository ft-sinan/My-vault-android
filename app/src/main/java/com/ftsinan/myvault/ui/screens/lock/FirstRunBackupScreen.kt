package com.ftsinan.myvault.ui.screens.lock

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.backup.BackupProtection
import com.ftsinan.myvault.ui.components.BackupOwnerSelector
import com.ftsinan.myvault.ui.components.MorphingShapeBox
import com.ftsinan.myvault.ui.components.ShieldKeyholeMark
import com.ftsinan.myvault.util.FormatUtils

/**
 * Asked once during first setup, before a PIN exists: restore an existing
 * `.mvbackup`, or start a new vault on this phone.
 */
@Composable
fun FirstRunBackupScreen(
    onCreateNewVault: () -> Unit,
    onRestoreComplete: () -> Unit,
    viewModel: FirstRunBackupViewModel = viewModel(factory = FirstRunBackupViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var restorePassword by remember { mutableStateOf("") }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onRestoreFileSelected(uri)
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == FirstRunBackupPhase.COMPLETED) {
            restorePassword = ""
            onRestoreComplete()
        }
    }

    if (uiState.phase == FirstRunBackupPhase.PASSWORD_REQUIRED) {
        AlertDialog(
            onDismissRequest = {
                restorePassword = ""
                viewModel.cancelRestore()
            },
            title = { Text(stringResource(R.string.backup_restore_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.backup_restore_password_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = {
                            restorePassword = it
                            viewModel.clearMessage()
                        },
                        label = { Text(stringResource(R.string.backup_password)) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    uiState.messageRes?.let { messageRes ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(messageRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val enteredPassword = restorePassword
                        restorePassword = ""
                        viewModel.prepareProtectedRestore(enteredPassword)
                    },
                    enabled = restorePassword.isNotEmpty()
                ) {
                    Text(stringResource(R.string.backup_check))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        restorePassword = ""
                        viewModel.cancelRestore()
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    val summary = uiState.summary
    if (
        summary != null &&
        (uiState.phase == FirstRunBackupPhase.READY ||
            uiState.phase == FirstRunBackupPhase.RESTORING)
    ) {
        AlertDialog(
            onDismissRequest = {
                if (uiState.phase == FirstRunBackupPhase.READY) {
                    viewModel.cancelRestore()
                }
            },
            title = { Text(stringResource(R.string.backup_restore_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(
                            R.string.backup_summary,
                            summary.memberCount,
                            summary.documentCount,
                            summary.pageCount
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (summary.createdAt > 0L) {
                        Text(
                            text = stringResource(
                                R.string.backup_created_on,
                                FormatUtils.formatDate(summary.createdAt)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = stringResource(
                            if (summary.protection == BackupProtection.PASSWORD) {
                                R.string.backup_password_protected
                            } else {
                                R.string.backup_unprotected
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.profileOptions.size > 1) {
                        Spacer(Modifier.height(14.dp))
                        BackupOwnerSelector(
                            options = uiState.profileOptions,
                            selectedBackupMemberId = uiState.selectedOwnerBackupId,
                            enabled = uiState.phase == FirstRunBackupPhase.READY,
                            onSelect = viewModel::selectRestoreOwner
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.first_run_backup_ready_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (uiState.phase == FirstRunBackupPhase.RESTORING) {
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.backup_restoring))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::commitRestore,
                    enabled = uiState.phase == FirstRunBackupPhase.READY &&
                        (uiState.profileOptions.isEmpty() ||
                            uiState.selectedOwnerBackupId != null)
                ) {
                    Text(stringResource(R.string.backup_restore))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::cancelRestore,
                    enabled = uiState.phase == FirstRunBackupPhase.READY
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ShieldKeyholeMark(size = 84.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.first_run_backup_question),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.first_run_backup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            FirstRunChoiceCard(
                icon = Icons.Filled.Restore,
                title = stringResource(R.string.first_run_restore_backup),
                subtitle = stringResource(R.string.first_run_restore_backup_subtitle),
                emphasized = true,
                enabled = !uiState.isBusy &&
                    uiState.phase != FirstRunBackupPhase.READY &&
                    uiState.phase != FirstRunBackupPhase.COMPLETED,
                onClick = {
                    viewModel.clearMessage()
                    restoreFileLauncher.launch(arrayOf("*/*"))
                }
            )
            Spacer(Modifier.height(12.dp))
            FirstRunChoiceCard(
                icon = Icons.Filled.Add,
                title = stringResource(R.string.first_run_create_new_vault),
                subtitle = stringResource(R.string.first_run_create_new_vault_subtitle),
                emphasized = false,
                enabled = !uiState.isBusy &&
                    uiState.phase != FirstRunBackupPhase.READY &&
                    uiState.phase != FirstRunBackupPhase.COMPLETED,
                onClick = {
                    viewModel.clearMessage()
                    onCreateNewVault()
                }
            )

            if (
                uiState.phase == FirstRunBackupPhase.INSPECTING ||
                uiState.phase == FirstRunBackupPhase.PREPARING
            ) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.backup_preparing),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.phase == FirstRunBackupPhase.IDLE) {
                uiState.messageRes?.let { messageRes ->
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(messageRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.messageIsError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FirstRunChoiceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    emphasized: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val titleColor: Color
    val subtitleColor: Color
    val chevronColor: Color
    if (emphasized) {
        titleColor = MaterialTheme.colorScheme.onPrimaryContainer
        subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
        chevronColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
    } else {
        titleColor = MaterialTheme.colorScheme.onSurface
        subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
        chevronColor = MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
        ),
        border = if (emphasized) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MorphingShapeBox(
                modifier = Modifier.size(46.dp),
                color = if (emphasized) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                lobes = 9,
                wave = 0.13f,
                spinMillis = 22_000
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (emphasized) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.size(23.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = chevronColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

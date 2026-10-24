package com.ftsinan.myvault.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.backup.BackupRestoreMode

/** Non-destructive add/merge is listed first and selected by the caller. */
@Composable
fun BackupRestoreModeSelector(
    selectedMode: BackupRestoreMode,
    enabled: Boolean,
    onSelect: (BackupRestoreMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.backup_restore_mode_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))

        listOf(
            BackupRestoreMode.ADD_TO_VAULT,
            BackupRestoreMode.REPLACE_VAULT
        ).forEach { mode ->
            val selected = mode == selectedMode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(mode) }
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    enabled = enabled
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            when (mode) {
                                BackupRestoreMode.ADD_TO_VAULT ->
                                    R.string.backup_restore_mode_add
                                BackupRestoreMode.REPLACE_VAULT ->
                                    R.string.backup_restore_mode_replace
                            }
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(
                            when (mode) {
                                BackupRestoreMode.ADD_TO_VAULT ->
                                    R.string.backup_restore_mode_add_subtitle
                                BackupRestoreMode.REPLACE_VAULT ->
                                    R.string.backup_restore_mode_replace_subtitle
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

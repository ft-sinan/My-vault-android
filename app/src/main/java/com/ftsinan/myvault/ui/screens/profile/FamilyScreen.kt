package com.ftsinan.myvault.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.ui.components.MemberAvatar
import com.ftsinan.myvault.ui.components.assignedGroupIcon
import com.ftsinan.myvault.ui.components.assignedGroupLabel
import com.ftsinan.myvault.ui.components.bloodGroupLabel
import com.ftsinan.myvault.ui.components.categoryColor
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.ui.components.relationLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    onProfileClick: (Long) -> Unit,
    onGroupClick: (AssignedGroup) -> Unit,
    onManageMembers: () -> Unit,
    viewModel: FamilyViewModel = viewModel(factory = FamilyViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        Text(stringResource(R.string.family_title),
                            style = MaterialTheme.typography.headlineLarge)
                        Text(
                            stringResource(R.string.family_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onManageMembers) {
                        Icon(
                            Icons.Filled.ManageAccounts,
                            contentDescription = stringResource(R.string.manage_members_title)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.members, key = { it.member.id }) { item ->
                MemberProfileCard(
                    item = item,
                    onClick = { onProfileClick(item.member.id) }
                )
            }

            item {
                Text(
                    stringResource(R.string.family_shared_groups),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(AssignedGroup.entries.chunked(2)) { rowGroups ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowGroups.forEach { group ->
                        GroupCard(
                            group = group,
                            count = uiState.groupCounts[group] ?: 0,
                            onClick = { onGroupClick(group) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowGroups.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
private fun MemberProfileCard(item: MemberListItem, onClick: () -> Unit) {
    val member = item.member
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (member.isOwner) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberAvatar(name = member.name, photoBytes = item.photoBytes, size = 62.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = relationLabel(member.relation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (item.documentCount == 1) {
                        stringResource(R.string.documents_count_one)
                    } else {
                        stringResource(R.string.documents_count_many, item.documentCount)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            member.bloodGroup?.takeIf { it.isNotBlank() }?.let { blood ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "🩸 ${bloodGroupLabel(blood)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun GroupCard(
    group: AssignedGroup,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when (group) {
        AssignedGroup.FAMILY -> categoryColor(Category.CERTIFICATES)
        AssignedGroup.VEHICLE -> categoryColor(Category.VEHICLE)
        AssignedGroup.HOME -> categoryColor(Category.PROPERTY)
        AssignedGroup.OTHER -> categoryColor(Category.OTHER)
    }
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(assignedGroupIcon(group), null, tint = accent, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(assignedGroupLabel(group), style = MaterialTheme.typography.titleMedium,
                maxLines = 1)
            Text(
                text = if (count == 1) stringResource(R.string.documents_count_one)
                else stringResource(R.string.documents_count_many, count),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

package com.ftsinan.myvault.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.ui.components.DocumentCard
import com.ftsinan.myvault.ui.components.EmptyState
import com.ftsinan.myvault.ui.components.MemberAvatar
import com.ftsinan.myvault.ui.components.bloodGroupLabel
import com.ftsinan.myvault.ui.screens.addedit.ImportSource
import com.ftsinan.myvault.util.FormatUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddDocument: (ImportSource) -> Unit,
    onDocumentClick: (Long) -> Unit,
    onProfileClick: (Long) -> Unit,
    onFamilyClick: () -> Unit,
    onLock: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
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
                        Text(
                            text = greetingText(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.owner?.member?.name
                                ?: stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLock) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.settings_lock_now)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            uiState.owner?.let { owner ->
                item {
                    OwnerProfileCard(
                        owner = owner,
                        totalCount = uiState.totalCount,
                        peopleCount = uiState.members.size,
                        totalSize = uiState.totalSize,
                        onClick = { onProfileClick(owner.member.id) }
                    )
                }
            }

            item {
                SectionTitle(stringResource(R.string.home_quick_actions))
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        icon = Icons.Filled.QrCodeScanner,
                        title = stringResource(R.string.add_scan),
                        subtitle = stringResource(R.string.add_scan_subtitle),
                        onClick = { onAddDocument(ImportSource.SCAN) },
                        modifier = Modifier.weight(1f),
                        primary = true
                    )
                    QuickActionCard(
                        icon = Icons.Filled.FolderOpen,
                        title = stringResource(R.string.add_file),
                        subtitle = stringResource(R.string.add_file_subtitle),
                        onClick = { onAddDocument(ImportSource.FILE) },
                        modifier = Modifier.weight(1f),
                        primary = false
                    )
                }
            }

            if (uiState.members.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(
                            stringResource(R.string.home_family),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onFamilyClick) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = null)
                        }
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(uiState.members, key = { it.member.id }) { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onProfileClick(item.member.id) }
                                    .padding(vertical = 6.dp)
                            ) {
                                MemberAvatar(
                                    name = item.member.name,
                                    photoBytes = item.photoBytes,
                                    size = 56.dp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = item.member.name.split(" ").firstOrNull() ?: "",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.totalCount == 0) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Shield,
                        title = stringResource(R.string.home_empty_title),
                        subtitle = stringResource(R.string.home_empty_subtitle),
                        actionLabel = stringResource(R.string.home_add_first),
                        onAction = { onAddDocument(ImportSource.SCAN) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                if (uiState.favourites.isNotEmpty()) {
                    item { SectionTitle(stringResource(R.string.home_favourites)) }
                    items(uiState.favourites, key = { "fav-${it.document.id}" }) { summary ->
                        DocumentCard(
                            summary = summary,
                            onClick = { onDocumentClick(summary.document.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                item { SectionTitle(stringResource(R.string.home_recent)) }
                items(uiState.recent, key = { "recent-${it.document.id}" }) { summary ->
                    DocumentCard(
                        summary = summary,
                        onClick = { onDocumentClick(summary.document.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
private fun greetingText(): String {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    return stringResource(
        when (hour) {
            in 5..11 -> R.string.home_greeting_morning
            in 12..16 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
    )
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(top = 2.dp)
    )
}

@Composable
private fun OwnerProfileCard(
    owner: MemberListItem,
    totalCount: Int,
    peopleCount: Int,
    totalSize: Long,
    onClick: () -> Unit
) {
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(
                        name = owner.member.name,
                        photoBytes = owner.photoBytes,
                        size = 58.dp
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = owner.member.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val bloodGroup = owner.member.bloodGroup
                        val bloodGroupText = if (bloodGroup != null) {
                            "🩸 ${bloodGroupLabel(bloodGroup)}"
                        } else {
                            null
                        }
                        val details = listOfNotNull(
                            bloodGroupText,
                            owner.member.dateOfBirth?.let { FormatUtils.formatDate(it) }
                        ).joinToString(" • ")
                        Text(
                            text = details.ifBlank {
                                stringResource(R.string.home_private_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                .copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomePill(Icons.Filled.Description, totalCount.toString(),
                        stringResource(R.string.stat_docs), Modifier.weight(1f))
                    HomePill(Icons.Filled.Star, owner.documentCount.toString(),
                        stringResource(R.string.stat_saved), Modifier.weight(1f))
                    HomePill(Icons.Filled.Groups, peopleCount.toString(),
                        stringResource(R.string.stat_people), Modifier.weight(1f))
                    HomePill(Icons.Filled.Storage, FormatUtils.formatFileSize(totalSize),
                        stringResource(R.string.stat_on_device), Modifier.weight(1.2f))
                }
            }
        }
    }
}

@Composable
private fun HomePill(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.62f))
            .padding(horizontal = 9.dp, vertical = 10.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.height(5.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (primary) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = if (primary) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (primary) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

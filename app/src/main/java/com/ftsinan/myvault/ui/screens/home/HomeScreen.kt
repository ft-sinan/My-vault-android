package com.ftsinan.myvault.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val listState = rememberLazyListState()
    val showProfileInTopBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 220
        }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    )
    val owner = uiState.owner

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Crossfade(
                        targetState = showProfileInTopBar && owner != null,
                        label = "homeTopBarProfile"
                    ) { showProfile ->
                        val currentOwner = owner
                        if (showProfile && currentOwner != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onProfileClick(currentOwner.member.id) }
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MemberAvatar(
                                    name = currentOwner.member.name,
                                    photoBytes = currentOwner.photoBytes,
                                    size = 36.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentOwner.member.name
                                            .substringBefore(" "),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = stringResource(R.string.home_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = stringResource(R.string.home_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
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
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
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
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Soft decorative shapes keep the compact banner from feeling flat.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-34).dp)
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.12f)
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-26).dp, y = 34.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.08f)
                    )
            )
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp)
                    .size(64.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLowest
                                    .copy(alpha = 0.52f)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceContainerLowest
                                    .copy(alpha = 0.75f),
                                shape = CircleShape
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MemberAvatar(
                            name = owner.member.name,
                            photoBytes = owner.photoBytes,
                            size = 62.dp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                            .align(Alignment.Top)
                            .weight(1f)
                    ) {
                        Text(
                            text = greetingText(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            text = owner.member.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
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
                                stringResource(R.string.home_private_encrypted)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                .copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLowest
                                    .copy(alpha = 0.46f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLowest
                                .copy(alpha = 0.24f)
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BannerStat(
                        icon = Icons.Filled.Description,
                        text = "$totalCount ${stringResource(R.string.stat_docs)}",
                        modifier = Modifier.weight(1f)
                    )
                    BannerStat(
                        icon = Icons.Filled.Groups,
                        text = "$peopleCount ${stringResource(R.string.stat_people)}",
                        modifier = Modifier.weight(1f)
                    )
                    BannerStat(
                        icon = Icons.Filled.Storage,
                        text = if (totalSize > 0) {
                            FormatUtils.formatFileSize(totalSize)
                        } else {
                            "0 B"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerStat(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

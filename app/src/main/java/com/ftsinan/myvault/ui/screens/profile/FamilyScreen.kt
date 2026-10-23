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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ManageAccounts
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.ui.components.CapsuleTopBar
import com.ftsinan.myvault.ui.components.HeaderCircleButton
import com.ftsinan.myvault.ui.components.MemberAvatar
import com.ftsinan.myvault.ui.components.MorphingShapeBox
import com.ftsinan.myvault.ui.components.expressivePastel
import com.ftsinan.myvault.ui.components.expressiveReveal
import com.ftsinan.myvault.ui.components.memberIdentitySeed
import com.ftsinan.myvault.ui.components.readableContentColor
import com.ftsinan.myvault.ui.components.bloodGroupLabel
import com.ftsinan.myvault.ui.components.relationLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    onProfileClick: (Long) -> Unit,
    onManageMembers: () -> Unit,
    viewModel: FamilyViewModel = viewModel(factory = FamilyViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CapsuleTopBar(
                content = {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.family_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            stringResource(R.string.family_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                trailing = {
                    HeaderCircleButton(
                        icon = Icons.Filled.ManageAccounts,
                        contentDescription = stringResource(R.string.manage_members_title),
                        onClick = onManageMembers
                    )
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.members.size.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
            if (uiState.members.isNotEmpty()) {
                item {
                    FamilySummaryHeader(
                        members = uiState.members,
                        modifier = Modifier.expressiveReveal(0, "family-summary")
                    )
                }
            }

            itemsIndexed(uiState.members, key = { _, it -> it.member.id }) { index, item ->
                MemberProfileCard(
                    item = item,
                    onClick = { onProfileClick(item.member.id) },
                    modifier = Modifier.expressiveReveal(index + 1, item.member.id)
                )
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}


@Composable
private fun FamilySummaryHeader(members: List<MemberListItem>, modifier: Modifier = Modifier) {
    val totalDocs = members.sumOf { it.documentCount }
    val headerSeed = members.firstOrNull { it.member.isOwner }?.let {
        memberIdentitySeed(it.member.id, it.member.name)
    } ?: MaterialTheme.colorScheme.primary
    val containerColor = expressivePastel(headerSeed)
    val contentColor = readableContentColor(containerColor)
    val mutedContentColor = contentColor.copy(alpha = 0.76f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(containerColor)
            .padding(16.dp)
    ) {
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-34).dp)
                .size(96.dp),
            color = headerSeed.copy(alpha = 0.18f),
            lobes = 12,
            wave = 0.18f,
            spinMillis = 24_000
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            MorphingShapeBox(
                modifier = Modifier.size(54.dp),
                color = headerSeed.copy(alpha = 0.28f),
                lobes = 9,
                wave = 0.14f
            ) {
                Icon(
                    Icons.Filled.Groups,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(27.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${members.size} ${stringResource(R.string.stat_people)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor
                )
                Text(
                    text = if (totalDocs == 1) {
                        stringResource(R.string.documents_count_one)
                    } else {
                        stringResource(R.string.documents_count_many, totalDocs)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedContentColor
                )
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    members.take(8).forEach { memberItem ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    memberIdentitySeed(
                                        memberItem.member.id,
                                        memberItem.member.name
                                    )
                                )
                        )
                    }
                }
            }
            val shown = members.take(4)
            Box(
                modifier = Modifier
                    .width((44 + (shown.size - 1).coerceAtLeast(0) * 29).dp)
                    .height(44.dp)
            ) {
                shown.forEachIndexed { index, memberItem ->
                    Box(
                        modifier = Modifier
                            .offset(x = (index * 29).dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(containerColor)
                            .padding(2.dp)
                    ) {
                        MemberAvatar(
                            name = memberItem.member.name,
                            photoBytes = memberItem.photoBytes,
                            size = 40.dp,
                            seedColor = memberIdentitySeed(
                                memberItem.member.id,
                                memberItem.member.name
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberProfileCard(item: MemberListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val member = item.member
    val seed = memberIdentitySeed(member.id, member.name)
    val containerColor = expressivePastel(seed)
    val contentColor = readableContentColor(containerColor)
    val mutedContentColor = contentColor.copy(alpha = 0.74f)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                MorphingShapeBox(
                    modifier = Modifier.size(68.dp),
                    color = seed.copy(alpha = 0.30f),
                    lobes = 12,
                    wave = 0.18f,
                    spinMillis = 22_000
                )
                MemberAvatar(
                    name = member.name,
                    photoBytes = item.photoBytes,
                    size = 58.dp,
                    seedColor = seed
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.Top)
                    .weight(1f)
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = relationLabel(member.relation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedContentColor
                )
                Text(
                    text = if (item.documentCount == 1) {
                        stringResource(R.string.documents_count_one)
                    } else {
                        stringResource(R.string.documents_count_many, item.documentCount)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = mutedContentColor
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
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = mutedContentColor
            )
        }
    }
}

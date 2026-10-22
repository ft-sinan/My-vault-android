package com.ftsinan.myvault.ui.screens.settings

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.ui.components.MemberAvatar
import com.ftsinan.myvault.ui.components.relationLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageMembersScreen(
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onEditMember: (Long) -> Unit,
    viewModel: ManageMembersViewModel = viewModel(factory = ManageMembersViewModel.Factory)
) {
    val allItems by viewModel.members.collectAsStateWithLifecycle()
    val owner = allItems.firstOrNull { it.member.isOwner }

    var localMembers by remember { mutableStateOf<List<MemberListItem>>(emptyList()) }
    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var memberToDelete by remember { mutableStateOf<MemberProfile?>(null) }

    LaunchedEffect(allItems, draggingId) {
        if (draggingId == null) {
            localMembers = allItems.filter { !it.member.isOwner }
        }
    }

    val stridePx = with(LocalDensity.current) { 94.dp.toPx() }

    fun maybeSwap(memberId: Long) {
        while (true) {
            val index = localMembers.indexOfFirst { it.member.id == memberId }
            if (index < 0) return
            when {
                dragOffsetPx > stridePx / 2 && index < localMembers.lastIndex -> {
                    localMembers = localMembers.toMutableList().also {
                        val item = it.removeAt(index)
                        it.add(index + 1, item)
                    }
                    dragOffsetPx -= stridePx
                }
                dragOffsetPx < -stridePx / 2 && index > 0 -> {
                    localMembers = localMembers.toMutableList().also {
                        val item = it.removeAt(index)
                        it.add(index - 1, item)
                    }
                    dragOffsetPx += stridePx
                }
                else -> return
            }
        }
    }

    fun finishDrag() {
        if (draggingId != null) {
            viewModel.reorder(localMembers.map { it.member })
        }
        draggingId = null
        dragOffsetPx = 0f
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                title = {
                    Column {
                        Text(stringResource(R.string.manage_members_title), maxLines = 1)
                        Text(
                            stringResource(R.string.manage_members_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            owner?.let { ownerItem ->
                item {
                    Text(
                        stringResource(R.string.manage_my_profile),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    MemberManageRow(
                        item = ownerItem,
                        draggable = false,
                        isDragging = false,
                        dragOffsetPx = 0f,
                        onEdit = { onEditMember(ownerItem.member.id) },
                        onDelete = null,
                        dragModifier = Modifier
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.family_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onAddMember,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.member_add))
                    }
                }
            }

            items(localMembers, key = { it.member.id }) { item ->
                val isDragging = draggingId == item.member.id
                val dragModifier = Modifier.pointerInput(item.member.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            draggingId = item.member.id
                            dragOffsetPx = 0f
                        },
                        onDragEnd = { finishDrag() },
                        onDragCancel = { finishDrag() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetPx += dragAmount.y
                            maybeSwap(item.member.id)
                        }
                    )
                }
                MemberManageRow(
                    item = item,
                    draggable = true,
                    isDragging = isDragging,
                    dragOffsetPx = if (isDragging) dragOffsetPx else 0f,
                    onEdit = { onEditMember(item.member.id) },
                    onDelete = { memberToDelete = item.member },
                    dragModifier = dragModifier
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text(stringResource(R.string.member_delete_title)) },
            text = { Text(stringResource(R.string.member_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(member)
                    memberToDelete = null
                }) {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun MemberManageRow(
    item: MemberListItem,
    draggable: Boolean,
    isDragging: Boolean,
    dragOffsetPx: Float,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
    dragModifier: Modifier
) {
    val member = item.member
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 6.dp else 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer { translationY = dragOffsetPx }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (draggable) {
                Box(
                    modifier = dragModifier.padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.DragHandle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            MemberAvatar(member.name, item.photoBytes, size = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    member.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    relationLabel(member.relation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

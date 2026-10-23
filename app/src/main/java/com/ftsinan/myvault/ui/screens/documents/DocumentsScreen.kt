package com.ftsinan.myvault.ui.screens.documents

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.ui.theme.animatedPlacement
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.ui.components.DocumentCard
import com.ftsinan.myvault.ui.components.EmptyState
import com.ftsinan.myvault.ui.components.assignedGroupLabel
import com.ftsinan.myvault.ui.components.categoryColor
import com.ftsinan.myvault.ui.components.categoryIcon
import com.ftsinan.myvault.ui.components.categoryLabel
import com.ftsinan.myvault.ui.screens.addedit.ImportSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    initialGroup: AssignedGroup? = null,
    onAddDocument: (ImportSource) -> Unit,
    onDocumentClick: (Long) -> Unit,
    viewModel: DocumentsViewModel = viewModel(factory = DocumentsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filters = uiState.filters
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    )

    LaunchedEffect(initialGroup) { viewModel.applyGroup(initialGroup) }

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
                    Column {
                        Text(
                            stringResource(R.string.docs_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(R.string.docs_shown, uiState.documents.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

        if (uiState.totalCount == 0) {
            EmptyState(
                icon = Icons.Filled.Shield,
                title = stringResource(R.string.home_empty_title),
                subtitle = stringResource(R.string.home_empty_subtitle),
                actionLabel = stringResource(R.string.home_add_first),
                onAction = { onAddDocument(ImportSource.SCAN) },
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = filters.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.docs_search_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filters.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
            }

            val usedCategories = Category.entries
                .filter { (uiState.categoryCounts[it] ?: 0) > 0 }
            if (usedCategories.isNotEmpty()) {
                item {
                    Text(stringResource(R.string.docs_categories),
                        style = MaterialTheme.typography.titleLarge)
                }
                items(usedCategories.chunked(2)) { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCategories.forEach { category ->
                            CategoryCard(
                                category = category,
                                count = uiState.categoryCounts[category] ?: 0,
                                selected = filters.category == category,
                                onClick = {
                                    viewModel.onCategorySelected(
                                        if (filters.category == category) null else category
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowCategories.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filters.memberId == null && filters.group == null &&
                                !filters.favouritesOnly,
                            onClick = {
                                viewModel.onMemberSelected(null)
                                viewModel.onFavouritesOnlyChange(false)
                            },
                            label = {
                                Text("${stringResource(R.string.docs_all)} (${uiState.totalCount})")
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = filters.favouritesOnly,
                            onClick = {
                                viewModel.onFavouritesOnlyChange(!filters.favouritesOnly)
                            },
                            label = { Text(stringResource(R.string.home_favourites)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Star, null, Modifier.size(16.dp))
                            }
                        )
                    }
                    items(uiState.members, key = { it.id }) { member ->
                        FilterChip(
                            selected = filters.memberId == member.id,
                            onClick = {
                                viewModel.onMemberSelected(
                                    if (filters.memberId == member.id) null else member.id
                                )
                            },
                            label = { Text(member.name.split(" ").firstOrNull() ?: member.name) }
                        )
                    }
                    items(AssignedGroup.entries) { group ->
                        FilterChip(
                            selected = filters.group == group,
                            onClick = {
                                viewModel.onGroupSelected(
                                    if (filters.group == group) null else group
                                )
                            },
                            label = { Text(assignedGroupLabel(group)) }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.docs_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (filters.isActive) {
                        TextButton(onClick = viewModel::clearFilters) {
                            Text(stringResource(R.string.docs_clear_filters))
                        }
                    }
                }
            }

            if (uiState.documents.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = stringResource(R.string.docs_no_match_title),
                        subtitle = stringResource(R.string.docs_no_match_subtitle),
                        actionLabel = stringResource(R.string.docs_clear_filters),
                        onAction = viewModel::clearFilters,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(uiState.documents, key = { it.document.id }) { summary ->
                    DocumentCard(
                        summary = summary,
                        onClick = { onDocumentClick(summary.document.id) },
                        modifier = animatedPlacement()
                    )
                }
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = categoryColor(category)
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.20f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = if (selected) BorderStroke(1.5.dp, color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(category), null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(categoryLabel(category), style = MaterialTheme.typography.titleMedium,
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

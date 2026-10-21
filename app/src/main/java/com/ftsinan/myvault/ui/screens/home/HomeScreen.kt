package com.ftsinan.myvault.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.ui.components.DocumentCard
import com.ftsinan.myvault.ui.components.EmptyState
import com.ftsinan.myvault.ui.components.categoryColor
import com.ftsinan.myvault.ui.components.categoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddDocument: () -> Unit,
    onDocumentClick: (Long) -> Unit,
    onSettings: () -> Unit,
    onLock: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My Vault", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onLock) {
                        Icon(Icons.Filled.Lock, contentDescription = "Lock vault now")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDocument) {
                Icon(Icons.Filled.Add, contentDescription = "Add document")
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.totalCount == 0 -> {
                EmptyState(
                    icon = Icons.Filled.Shield,
                    title = "Your vault is empty",
                    subtitle = "Add your Aadhaar, PAN, certificates and other " +
                        "important documents. Everything is encrypted and " +
                        "stays only on this phone.",
                    actionLabel = "Add your first document",
                    onAction = onAddDocument,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            else -> {
                HomeContent(
                    uiState = uiState,
                    onDocumentClick = onDocumentClick,
                    onSearchChange = viewModel::onSearchChange,
                    onCategorySelected = viewModel::onCategorySelected,
                    onRelationSelected = viewModel::onRelationSelected,
                    onFavouritesOnlyChange = viewModel::onFavouritesOnlyChange,
                    onClearFilters = viewModel::clearFilters,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onDocumentClick: (Long) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onRelationSelected: (Relation?) -> Unit,
    onFavouritesOnlyChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = uiState.filters

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = filters.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Aadhaar, PAN, Appu…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (filters.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        if (uiState.expiryAlerts.isNotEmpty()) {
            item { ExpiryAlertBanner(uiState.expiryAlerts, onDocumentClick) }
        }

        // Category grid (only categories that actually hold documents)
        val usedCategories = Category.entries.filter { (uiState.categoryCounts[it] ?: 0) > 0 }
        if (usedCategories.isNotEmpty()) {
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(usedCategories.chunked(2)) { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCategories.forEach { category ->
                        CategoryCard(
                            category = category,
                            count = uiState.categoryCounts[category] ?: 0,
                            selected = filters.category == category,
                            onClick = {
                                onCategorySelected(
                                    if (filters.category == category) null else category
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowCategories.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Filter chips: All / Favourites / per family member
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = filters.relation == null && !filters.favouritesOnly,
                        onClick = {
                            onRelationSelected(null)
                            onFavouritesOnlyChange(false)
                        },
                        label = { Text("All (${uiState.totalCount})") }
                    )
                }
                item {
                    FilterChip(
                        selected = filters.favouritesOnly,
                        onClick = { onFavouritesOnlyChange(!filters.favouritesOnly) },
                        label = { Text("Favourites") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                items(uiState.relationsInUse) { relation ->
                    FilterChip(
                        selected = filters.relation == relation,
                        onClick = {
                            onRelationSelected(
                                if (filters.relation == relation) null else relation
                            )
                        },
                        label = { Text(relation.label) }
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
                    text = "Documents",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (filters.isActive) {
                    TextButton(onClick = onClearFilters) { Text("Clear filters") }
                }
            }
        }

        if (uiState.documents.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.SearchOff,
                    title = "Nothing matches",
                    subtitle = "Try a different search, or clear the filters.",
                    actionLabel = "Clear filters",
                    onAction = onClearFilters,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(uiState.documents, key = { it.id }) { document ->
                DocumentCard(
                    document = document,
                    onClick = { onDocumentClick(document.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.22f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(category),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = if (count == 1) "1 document" else "$count documents",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExpiryAlertBanner(
    alerts: List<VaultDocument>,
    onDocumentClick: (Long) -> Unit
) {
    Card(
        onClick = { alerts.firstOrNull()?.let { onDocumentClick(it.id) } },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (alerts.size == 1) "1 document needs attention"
                    else "${alerts.size} documents need attention",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = alerts.joinToString(", ") { it.docType }
                        .let { if (it.length > 60) it.take(57) + "…" else it },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

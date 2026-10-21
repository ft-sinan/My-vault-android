package com.ftsinan.myvault.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.Relation
import com.ftsinan.myvault.data.model.VaultDocument
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.util.ExpiryStatus
import com.ftsinan.myvault.util.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class HomeFilters(
    val searchQuery: String = "",
    val category: Category? = null,
    val relation: Relation? = null,
    val favouritesOnly: Boolean = false
) {
    val isActive: Boolean
        get() = searchQuery.isNotBlank() || category != null ||
            relation != null || favouritesOnly
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val documents: List<VaultDocument> = emptyList(),
    val totalCount: Int = 0,
    val categoryCounts: Map<Category, Int> = emptyMap(),
    val relationsInUse: List<Relation> = emptyList(),
    /** Documents expired or expiring within 30 days, most urgent first. */
    val expiryAlerts: List<VaultDocument> = emptyList(),
    val filters: HomeFilters = HomeFilters()
)

class HomeViewModel(
    repository: DocumentRepository
) : ViewModel() {

    private val filters = MutableStateFlow(HomeFilters())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.documents,
        filters
    ) { allDocuments, activeFilters ->
        HomeUiState(
            isLoading = false,
            documents = allDocuments.filter { it.matches(activeFilters) },
            totalCount = allDocuments.size,
            categoryCounts = allDocuments.groupingBy { it.category }.eachCount(),
            relationsInUse = allDocuments.map { it.relation }.distinct()
                .sortedBy { it.ordinal },
            expiryAlerts = allDocuments
                .filter {
                    when (FormatUtils.expiryStatus(it.expiryDate)) {
                        is ExpiryStatus.Expired, is ExpiryStatus.ExpiringSoon -> true
                        else -> false
                    }
                }
                .sortedBy { it.expiryDate },
            filters = activeFilters
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private fun VaultDocument.matches(filters: HomeFilters): Boolean {
        if (filters.category != null && category != filters.category) return false
        if (filters.relation != null && relation != filters.relation) return false
        if (filters.favouritesOnly && !isFavourite) return false
        val query = filters.searchQuery.trim()
        if (query.isBlank()) return true
        val searchable = listOfNotNull(
            title, docType, holderName, documentNumber, notes, category.label
        ).joinToString(" ")
        return searchable.contains(query, ignoreCase = true)
    }

    fun onSearchChange(query: String) =
        filters.update { it.copy(searchQuery = query) }

    fun onCategorySelected(category: Category?) =
        filters.update { it.copy(category = category) }

    fun onRelationSelected(relation: Relation?) =
        filters.update { it.copy(relation = relation) }

    fun onFavouritesOnlyChange(enabled: Boolean) =
        filters.update { it.copy(favouritesOnly = enabled) }

    fun clearFilters() {
        filters.value = HomeFilters()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                HomeViewModel(app.container.documentRepository)
            }
        }
    }
}

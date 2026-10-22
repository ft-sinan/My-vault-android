package com.ftsinan.myvault.ui.screens.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.DocumentSummary
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class DocumentsFilters(
    val searchQuery: String = "",
    val category: Category? = null,
    val memberId: Long? = null,
    val group: AssignedGroup? = null,
    val favouritesOnly: Boolean = false
) {
    val isActive: Boolean
        get() = searchQuery.isNotBlank() || category != null || memberId != null ||
            group != null || favouritesOnly
}

data class DocumentsUiState(
    val isLoading: Boolean = true,
    val documents: List<DocumentSummary> = emptyList(),
    val totalCount: Int = 0,
    val categoryCounts: Map<Category, Int> = emptyMap(),
    val members: List<MemberProfile> = emptyList(),
    val filters: DocumentsFilters = DocumentsFilters()
)

class DocumentsViewModel(repository: DocumentRepository) : ViewModel() {

    private val filters = MutableStateFlow(DocumentsFilters())

    val uiState: StateFlow<DocumentsUiState> = combine(
        repository.documentSummaries,
        repository.members,
        filters
    ) { summaries, members, activeFilters ->
        DocumentsUiState(
            isLoading = false,
            documents = summaries.filter { it.matches(activeFilters) },
            totalCount = summaries.size,
            categoryCounts = summaries.groupingBy { it.document.category }.eachCount(),
            members = members,
            filters = activeFilters
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DocumentsUiState()
    )

    private fun DocumentSummary.matches(filters: DocumentsFilters): Boolean {
        val doc = document
        if (filters.category != null && doc.category != filters.category) return false
        if (filters.memberId != null && doc.memberId != filters.memberId) return false
        if (filters.group != null && doc.assignedGroup != filters.group) return false
        if (filters.favouritesOnly && !doc.isFavourite) return false
        val query = filters.searchQuery.trim()
        if (query.isBlank()) return true
        val searchable = listOfNotNull(
            doc.title, doc.docType, assignedName, doc.documentNumber,
            doc.category.label, doc.assignedGroup?.label, member?.name
        ).joinToString(" ")
        return searchable.contains(query, ignoreCase = true)
    }

    fun onSearchChange(query: String) = filters.update { it.copy(searchQuery = query) }
    fun onCategorySelected(category: Category?) =
        filters.update { it.copy(category = category) }

    fun onMemberSelected(memberId: Long?) =
        filters.update { it.copy(memberId = memberId, group = null) }

    fun onGroupSelected(group: AssignedGroup?) =
        filters.update { it.copy(group = group, memberId = null) }

    fun onFavouritesOnlyChange(enabled: Boolean) =
        filters.update { it.copy(favouritesOnly = enabled) }

    fun applyGroup(group: AssignedGroup?) {
        if (group != null) onGroupSelected(group)
    }

    fun clearFilters() {
        filters.value = DocumentsFilters()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                DocumentsViewModel(app.container.documentRepository)
            }
        }
    }
}

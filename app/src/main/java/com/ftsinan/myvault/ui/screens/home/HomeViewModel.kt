package com.ftsinan.myvault.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.DocumentSummary
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.data.repository.DocumentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = true,
    val owner: MemberListItem? = null,
    val members: List<MemberListItem> = emptyList(),
    val recent: List<DocumentSummary> = emptyList(),
    val favourites: List<DocumentSummary> = emptyList(),
    val totalCount: Int = 0,
    val totalSize: Long = 0L
)

class HomeViewModel(repository: DocumentRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.memberListItems,
        repository.documentSummaries
    ) { members, summaries ->
        HomeUiState(
            isLoading = false,
            owner = members.firstOrNull { it.member.isOwner },
            members = members,
            recent = summaries.sortedByDescending { it.document.updatedAt }.take(5),
            favourites = summaries.filter { it.document.isFavourite }.take(5),
            totalCount = summaries.size,
            totalSize = summaries.sumOf { it.totalSize }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                HomeViewModel(app.container.documentRepository)
            }
        }
    }
}

package com.ftsinan.myvault.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.data.repository.DocumentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FamilyUiState(
    val isLoading: Boolean = true,
    val members: List<MemberListItem> = emptyList(),
    val groupCounts: Map<AssignedGroup, Int> = emptyMap()
)

class FamilyViewModel(repository: DocumentRepository) : ViewModel() {

    val uiState: StateFlow<FamilyUiState> = combine(
        repository.memberListItems,
        repository.documentSummaries
    ) { members, summaries ->
        FamilyUiState(
            isLoading = false,
            members = members,
            groupCounts = summaries
                .mapNotNull { it.document.assignedGroup }
                .groupingBy { it }
                .eachCount()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FamilyUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                FamilyViewModel(app.container.documentRepository)
            }
        }
    }
}

package com.ftsinan.myvault.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.data.model.MemberProfile
import com.ftsinan.myvault.data.repository.DocumentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageMembersViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    val members: StateFlow<List<MemberListItem>> = repository.memberListItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(member: MemberProfile) {
        viewModelScope.launch { repository.deleteMember(member) }
    }

    fun reorder(orderedMembers: List<MemberProfile>) {
        viewModelScope.launch { repository.updateMemberOrder(orderedMembers) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                ManageMembersViewModel(app.container.documentRepository)
            }
        }
    }
}

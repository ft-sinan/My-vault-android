package com.ftsinan.myvault.ui.screens.profile

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.DocumentSummary
import com.ftsinan.myvault.data.model.MemberListItem
import com.ftsinan.myvault.data.settings.LanguageManager
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.util.ProfileShareCardGenerator
import com.ftsinan.myvault.util.ProfileShareLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class ProfileDetailViewModel(
    application: Application,
    private val repository: DocumentRepository,
    private val memberId: Long
) : AndroidViewModel(application) {

    val memberItem: StateFlow<MemberListItem?> = repository.memberListItems
        .map { items -> items.firstOrNull { it.member.id == memberId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val documents: StateFlow<List<DocumentSummary>> = repository.documentSummaries
        .map { summaries -> summaries.filter { it.document.memberId == memberId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Creates the clean profile ID image (all available details,
     * no relation, no branding) and returns a shareable URI.
     */
    suspend fun prepareShareImage(): Uri? {
        val item = memberItem.value ?: return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val app = getApplication<Application>()
                val localizedContext = LanguageManager.wrap(app)
                val labels = ProfileShareLabels(
                    dateOfBirth = localizedContext.getString(R.string.field_dob),
                    gender = localizedContext.getString(R.string.field_gender),
                    bloodGroup = localizedContext.getString(R.string.field_blood_group),
                    genderValues = mapOf(
                        "Male" to localizedContext.getString(R.string.gender_male),
                        "Female" to localizedContext.getString(R.string.gender_female),
                        "Other" to localizedContext.getString(R.string.gender_other),
                        "Prefer not to say" to localizedContext.getString(
                            R.string.gender_prefer_not_to_say
                        )
                    ),
                    unknownBloodGroup = localizedContext.getString(
                        R.string.blood_group_unknown
                    )
                )
                val file = ProfileShareCardGenerator(repository.fileManager)
                    .create(item.member, item.photoBytes, labels)
                FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
            }.getOrNull()
        }
    }

    companion object {
        fun factory(memberId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                ProfileDetailViewModel(
                    application = app,
                    repository = app.container.documentRepository,
                    memberId = memberId
                )
            }
        }
    }
}

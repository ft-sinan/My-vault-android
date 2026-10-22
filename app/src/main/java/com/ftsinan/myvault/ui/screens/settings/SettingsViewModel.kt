package com.ftsinan.myvault.ui.screens.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ftsinan.myvault.MyVaultApplication
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.data.settings.AppLanguage
import com.ftsinan.myvault.data.settings.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsMessage(
    @StringRes val textRes: Int,
    val isError: Boolean,
    val formatArg: Int? = null
)

class SettingsViewModel(
    private val pinManager: PinManager,
    repository: DocumentRepository,
    languageManager: LanguageManager
) : ViewModel() {

    val language: StateFlow<AppLanguage?> =
        languageManager.language

    val biometricEnabled: StateFlow<Boolean> = pinManager.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val dynamicColorEnabled: StateFlow<Boolean> = pinManager.useDynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val documentCount: StateFlow<Int> = repository.documentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalSize: StateFlow<Long> = repository.totalSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _message = MutableStateFlow<SettingsMessage?>(null)
    val message: StateFlow<SettingsMessage?> = _message.asStateFlow()

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { pinManager.setBiometricEnabled(enabled) }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch { pinManager.setUseDynamicColor(enabled) }
    }

    fun changePin(currentPin: String, newPin: String, confirmPin: String) {
        viewModelScope.launch {
            _message.value = when {
                newPin.length != PinManager.PIN_LENGTH || newPin.any { !it.isDigit() } ->
                    SettingsMessage(
                        R.string.settings_pin_length_error,
                        isError = true,
                        formatArg = PinManager.PIN_LENGTH
                    )
                newPin != confirmPin ->
                    SettingsMessage(R.string.settings_pin_mismatch_error, isError = true)
                !pinManager.verifyPin(currentPin) ->
                    SettingsMessage(R.string.settings_current_pin_wrong, isError = true)
                else -> {
                    pinManager.setPin(newPin)
                    SettingsMessage(R.string.settings_pin_changed, isError = false)
                }
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyVaultApplication
                SettingsViewModel(
                    pinManager = app.container.pinManager,
                    repository = app.container.documentRepository,
                    languageManager = app.container.languageManager
                )
            }
        }
    }
}

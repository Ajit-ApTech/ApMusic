package com.example.musicplayer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.provider.MusicProvider
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.util.AudioEffectManager
import com.example.musicplayer.util.EqualizerState
import com.example.musicplayer.util.EncryptedPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderUiState(
    val providerId: String,
    val displayName: String,
    val requiresApiKey: Boolean,
    val currentKey: String,
    val inputKey: String,
    val status: String,
    val isTesting: Boolean = false,
    val testMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val encryptedPrefs: EncryptedPrefs,
    val audioEffectManager: AudioEffectManager
) : ViewModel() {

    val eqState: StateFlow<EqualizerState> = audioEffectManager.state

    private val _activeProviderId = MutableStateFlow(encryptedPrefs.getActiveProviderId())
    val activeProviderId: StateFlow<String> = _activeProviderId.asStateFlow()

    private val _providersState = MutableStateFlow<List<ProviderUiState>>(emptyList())
    val providersState: StateFlow<List<ProviderUiState>> = _providersState.asStateFlow()

    init {
        loadProviders()
    }

    private fun loadProviders() {
        val list = repository.getSupportedProviders().map { provider ->
            val key = encryptedPrefs.getApiKey(provider.providerId) ?: ""
            val status = encryptedPrefs.getProviderStatus(provider.providerId)
            ProviderUiState(
                providerId = provider.providerId,
                displayName = provider.displayName,
                requiresApiKey = provider.requiresApiKey,
                currentKey = key,
                inputKey = key,
                status = status
            )
        }
        _providersState.value = list
    }

    fun onKeyInputChanged(providerId: String, newKey: String) {
        _providersState.value = _providersState.value.map {
            if (it.providerId == providerId) it.copy(inputKey = newKey, testMessage = null) else it
        }
    }

    fun selectActiveProvider(providerId: String) {
        repository.setActiveProvider(providerId)
        _activeProviderId.value = providerId
    }

    fun saveKey(providerId: String) {
        val state = _providersState.value.firstOrNull { it.providerId == providerId } ?: return
        encryptedPrefs.setApiKey(providerId, state.inputKey)
        val newStatus = if (state.inputKey.isNotBlank()) "Active" else {
            if (state.requiresApiKey) "Not Configured" else "Active"
        }
        encryptedPrefs.setProviderStatus(providerId, newStatus)
        _providersState.value = _providersState.value.map {
            if (it.providerId == providerId) {
                it.copy(
                    currentKey = state.inputKey,
                    status = newStatus,
                    testMessage = "Saved successfully"
                )
            } else it
        }
    }

    fun deleteKey(providerId: String) {
        encryptedPrefs.deleteApiKey(providerId)
        val state = _providersState.value.firstOrNull { it.providerId == providerId }
        val newStatus = if (state?.requiresApiKey == true) "Not Configured" else "Active"
        encryptedPrefs.setProviderStatus(providerId, newStatus)
        _providersState.value = _providersState.value.map {
            if (it.providerId == providerId) {
                it.copy(
                    inputKey = "",
                    currentKey = "",
                    status = newStatus,
                    testMessage = "API Key deleted"
                )
            } else it
        }
    }

    fun testConnection(providerId: String) {
        val provider = repository.getSupportedProviders().firstOrNull { it.providerId == providerId } ?: return
        val currentInput = _providersState.value.firstOrNull { it.providerId == providerId }?.inputKey ?: ""

        _providersState.value = _providersState.value.map {
            if (it.providerId == providerId) it.copy(isTesting = true, testMessage = null) else it
        }

        viewModelScope.launch {
            val success = provider.testConnection(currentInput)
            val newStatus = if (success) "Active" else "Invalid"
            encryptedPrefs.setProviderStatus(providerId, newStatus)

            _providersState.value = _providersState.value.map {
                if (it.providerId == providerId) {
                    it.copy(
                        isTesting = false,
                        status = newStatus,
                        testMessage = if (success) "Connection successful!" else "Connection failed. Please verify the key."
                    )
                } else it
            }
        }
    }
}

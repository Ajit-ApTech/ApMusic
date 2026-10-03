package com.example.musicplayer.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPrefs @Inject constructor(@ApplicationContext private val context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val prefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _activeProviderFlow = MutableStateFlow(getActiveProviderId())
    val activeProviderFlow: StateFlow<String> = _activeProviderFlow.asStateFlow()

    fun getApiKey(provider: String): String? = prefs.getString("${provider}_api_key", null)

    fun setApiKey(provider: String, key: String) {
        prefs.edit().putString("${provider}_api_key", key).apply()
    }

    fun deleteApiKey(provider: String) {
        prefs.edit().remove("${provider}_api_key").apply()
    }

    fun getActiveProviderId(): String = prefs.getString("active_provider_id", "jiosaavn") ?: "jiosaavn"

    fun setActiveProviderId(providerId: String) {
        prefs.edit().putString("active_provider_id", providerId).apply()
        _activeProviderFlow.value = providerId
    }

    fun getProviderStatus(providerId: String): String {
        return prefs.getString("${providerId}_status", null) ?: run {
            if (providerId in listOf("jiosaavn", "ytmusic", "audius", "fma")) "Active" else "Not Configured"
        }
    }

    fun setProviderStatus(providerId: String, status: String) {
        prefs.edit().putString("${providerId}_status", status).apply()
    }

    fun getLastTrackId(): String? = prefs.getString("last_track_id", null)
    fun setLastTrackId(trackId: String?) {
        prefs.edit().putString("last_track_id", trackId).apply()
    }

    fun getLastPositionMs(): Long = prefs.getLong("last_position_ms", 0L)
    fun setLastPositionMs(positionMs: Long) {
        prefs.edit().putLong("last_position_ms", positionMs).apply()
    }
}

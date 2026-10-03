package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.util.EncryptedPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JamendoProvider @Inject constructor(
    private val api: JamendoApi,
    private val encryptedPrefs: EncryptedPrefs
) : MusicProvider {

    override val providerId: String = "jamendo"
    override val displayName: String = "Jamendo Music"
    override val requiresApiKey: Boolean = true

    private fun getClientId(): String = encryptedPrefs.getApiKey(providerId) ?: ""

    override suspend fun search(query: String): List<Track> {
        val clientId = getClientId()
        if (clientId.isBlank()) return emptyList()
        return try {
            val response = api.searchTracks(clientId = clientId, query = query)
            response.results.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrending(): List<Track> {
        val clientId = getClientId()
        if (clientId.isBlank()) return emptyList()
        return try {
            val response = api.getTracks(clientId = clientId)
            response.results.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrackStreamUrl(trackId: String): String {
        return ""
    }

    override suspend fun testConnection(apiKey: String?): Boolean {
        val keyToTest = apiKey ?: getClientId()
        if (keyToTest.isBlank()) return false
        return try {
            val response = api.getTracks(clientId = keyToTest, limit = 1)
            response.headers?.status == "success"
        } catch (e: Exception) {
            false
        }
    }

    private fun JamendoTrack.toDomain(): Track = Track(
        id = id,
        title = name.ifBlank { "Untitled Track" },
        artist = artistName.ifBlank { "Unknown Artist" },
        artworkUrl = image,
        durationMs = duration * 1000,
        streamUrl = audio
    )
}

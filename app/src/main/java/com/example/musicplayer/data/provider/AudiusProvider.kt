package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.util.EncryptedPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiusProvider @Inject constructor(
    private val api: AudiusApi,
    private val encryptedPrefs: EncryptedPrefs
) : MusicProvider {

    override val providerId: String = "audius"
    override val displayName: String = "Audius"
    override val requiresApiKey: Boolean = false

    private fun getEffectiveAppName(): String {
        val key = encryptedPrefs.getApiKey(providerId)
        return if (!key.isNullOrBlank()) key else "MusicPlayerApp"
    }

    override suspend fun search(query: String): List<Track> {
        return try {
            val response = api.search(query = query, appName = getEffectiveAppName())
            response.data.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrending(): List<Track> {
        return try {
            val response = api.getTrending(appName = getEffectiveAppName())
            response.data.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrackStreamUrl(trackId: String): String {
        val appName = getEffectiveAppName()
        val key = encryptedPrefs.getApiKey(providerId)
        val keyParam = if (!key.isNullOrBlank()) "&api_key=$key" else ""
        return "https://discoveryprovider.audius.co/v1/tracks/$trackId/stream?app_name=$appName$keyParam"
    }

    override suspend fun testConnection(apiKey: String?): Boolean {
        return try {
            val appName = if (!apiKey.isNullOrBlank()) apiKey else "MusicPlayerApp"
            val response = api.getTrending(appName = appName, limit = 1)
            response.data.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun AudiusTrack.toDomain(): Track {
        val artworkMap = artwork
        val artworkUrl = artworkMap?.get("480x480") as? String
            ?: artworkMap?.get("150x150") as? String
            ?: artworkMap?.get("1000x1000") as? String

        return Track(
            id = id,
            title = title.ifBlank { "Untitled Track" },
            artist = user.name.ifBlank { "Unknown Artist" },
            artworkUrl = artworkUrl,
            durationMs = duration * 1000,
            streamUrl = "https://discoveryprovider.audius.co/v1/tracks/$id/stream?app_name=MusicPlayerApp"
        )
    }
}

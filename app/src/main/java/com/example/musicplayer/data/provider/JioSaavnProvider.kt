package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JioSaavnProvider @Inject constructor(
    private val api: JioSaavnApi
) : MusicProvider {

    override val providerId: String = "jiosaavn"
    override val displayName: String = "JioSaavn (Bollywood & Indian)"
    override val requiresApiKey: Boolean = false

    override suspend fun search(query: String): List<Track> {
        val q = query.ifBlank { "hindi trending" }
        return try {
            val response = api.searchSongs(q)
            response.results.mapNotNull { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrending(): List<Track> {
        return try {
            val response = api.searchSongs("hindi trending songs 2026")
            response.results.mapNotNull { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrackStreamUrl(trackId: String): String {
        val cleanId = trackId.removePrefix("jiosaavn_")
        return try {
            val searchRes = api.searchSongs(cleanId)
            val matched = searchRes.results.firstOrNull { it.id == cleanId }
            if (matched != null) {
                JioSaavnUtils.decryptMediaUrl(matched.encryptedMediaUrl) ?: ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun getSimilarTracks(track: Track): List<Track> {
        // Extract just the first listed artist (JioSaavn lists multiple as "A, B, C")
        val primaryArtist = track.artist.split(",").first().trim().ifBlank { "hindi hits" }
        return try {
            val response = api.searchSongs(primaryArtist)
            response.results
                .mapNotNull { it.toDomain() }
                .filter { it.id != track.id }  // exclude the currently playing song
                .take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun testConnection(apiKey: String?): Boolean {
        return try {
            val response = api.searchSongs("arijit")
            response.results.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun JioSaavnSong.toDomain(): Track? {
        val stream = JioSaavnUtils.decryptMediaUrl(encryptedMediaUrl)
            ?: return null

        val title = JioSaavnUtils.cleanText(song).ifBlank { "Untitled Song" }
        val artist = JioSaavnUtils.cleanText(primaryArtists.ifBlank { singers }).ifBlank { "Bollywood Artist" }
        val artwork = JioSaavnUtils.formatImageUrl(image)
        val durationSec = duration.toIntOrNull() ?: 180

        return Track(
            id = id,
            title = title,
            artist = artist,
            artworkUrl = artwork,
            durationMs = durationSec * 1000,
            streamUrl = stream
        )
    }
}

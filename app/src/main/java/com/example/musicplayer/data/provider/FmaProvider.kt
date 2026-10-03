package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FmaProvider @Inject constructor(
    private val api: FmaApi
) : MusicProvider {

    override val providerId: String = "fma"
    override val displayName: String = "Free Music Archive"
    override val requiresApiKey: Boolean = false

    override suspend fun search(query: String): List<Track> {
        val q = if (query.isBlank()) {
            "collection:(freemusicarchive) AND mediatype:(audio)"
        } else {
            "collection:(freemusicarchive) AND mediatype:(audio) AND (${query})"
        }
        return try {
            val response = api.searchFma(q = q)
            response.response?.docs?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrending(): List<Track> {
        val q = "collection:(freemusicarchive) AND mediatype:(audio)"
        return try {
            val response = api.searchFma(q = q, rows = 30)
            response.response?.docs?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrackStreamUrl(trackId: String): String {
        return "https://archive.org/download/$trackId/${trackId}_vbr.mp3"
    }

    override suspend fun testConnection(apiKey: String?): Boolean {
        return try {
            val response = api.searchFma(q = "collection:(freemusicarchive)", rows = 1)
            (response.response?.numFound ?: 0) > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun ArchiveDoc.toDomain(): Track {
        val stream = "https://archive.org/download/$identifier/${identifier}_vbr.mp3"
        val artwork = "https://archive.org/services/img/$identifier"
        return Track(
            id = identifier,
            title = getResolvedTitle(),
            artist = getResolvedCreator(),
            artworkUrl = artwork,
            durationMs = 180000,
            streamUrl = stream
        )
    }
}

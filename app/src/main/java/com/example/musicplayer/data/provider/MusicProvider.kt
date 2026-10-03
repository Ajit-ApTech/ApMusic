package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Track

interface MusicProvider {
    val providerId: String
    val displayName: String
    val requiresApiKey: Boolean

    suspend fun search(query: String): List<Track>
    suspend fun getTrending(): List<Track>
    suspend fun getTrackStreamUrl(trackId: String): String
    suspend fun testConnection(apiKey: String?): Boolean

    /**
     * Returns a list of songs similar to [track].
     * Default implementation searches by the first artist name and excludes the current track.
     * Individual providers can override for smarter recommendations.
     */
    suspend fun getSimilarTracks(track: Track): List<Track> {
        val artistQuery = track.artist.split(",").first().trim().ifBlank { track.title }
        return try {
            search(artistQuery).filter { it.id != track.id }.take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.data.repository.Category

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

    // NEW: Album operations
    suspend fun getAlbum(albumId: String): Album? {
        return null // Override in implementations
    }

    suspend fun getAlbumTracks(albumId: String): List<Track> {
        return emptyList() // Override in implementations
    }

    suspend fun getArtistAlbums(artistId: String): List<Album> {
        return emptyList() // Override in implementations
    }

    suspend fun getNewReleases(): List<Album> {
        return emptyList() // Override in implementations
    }

    suspend fun searchAlbums(query: String): List<Album> {
        return emptyList() // Override in implementations
    }

    // NEW: Artist operations
    suspend fun getArtist(artistId: String): Artist? {
        return null // Override in implementations
    }

    suspend fun getArtistTopTracks(artistId: String): List<Track> {
        return emptyList() // Override in implementations
    }

    suspend fun getSimilarArtists(artistId: String): List<Artist> {
        return emptyList() // Override in implementations
    }

    suspend fun searchArtists(query: String): List<Artist> {
        return emptyList() // Override in implementations
    }

    // NEW: Browse/Discover
    suspend fun getFeaturedPlaylists(): List<Playlist> {
        return emptyList() // Override in implementations
    }

    suspend fun getCategories(): List<Category> {
        return emptyList() // Override in implementations
    }

    suspend fun getCategoryPlaylists(categoryId: String): List<Playlist> {
        return emptyList() // Override in implementations
    }

    suspend fun getRecommendations(seedTracks: List<String>, seedArtists: List<String>): List<Track> {
        return emptyList() // Override in implementations
    }

    // Lyrics
    suspend fun getLyrics(trackId: String): String? {
        return null // Override in implementations
    }
}
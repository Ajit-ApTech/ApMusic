package com.example.musicplayer.data.repository

import com.example.musicplayer.data.provider.MusicProvider
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MusicRepository {
    val activeProviderFlow: StateFlow<MusicProvider>
    fun getSupportedProviders(): List<MusicProvider>
    fun setActiveProvider(providerId: String)
    fun getActiveProvider(): MusicProvider

    suspend fun search(query: String): List<Track>
    suspend fun getTrending(): List<Track>
    suspend fun getTrackStreamUrl(track: Track): String
    suspend fun getSimilarTracks(track: Track): List<Track>

    // NEW: Album operations
    suspend fun getAlbum(albumId: String): Album?
    suspend fun getAlbumTracks(albumId: String): List<Track>
    suspend fun getArtistAlbums(artistId: String): List<Album>
    suspend fun getNewReleases(): List<Album>
    suspend fun searchAlbums(query: String): List<Album>

    // NEW: Artist operations
    suspend fun getArtist(artistId: String): Artist?
    suspend fun getArtistTopTracks(artistId: String): List<Track>
    suspend fun getSimilarArtists(artistId: String): List<Artist>
    suspend fun searchArtists(query: String): List<Artist>

    // NEW: Browse/Discover
    suspend fun getFeaturedPlaylists(): List<Playlist>
    suspend fun getCategories(): List<Category>
    suspend fun getCategoryPlaylists(categoryId: String): List<Playlist>
    suspend fun getRecommendations(seedTracks: List<String>, seedArtists: List<String>): List<Track>

    // Lyrics
    suspend fun getLyrics(trackId: String): String?

    // Queue persistence
    fun getPersistedQueue(): Flow<List<Track>>
    suspend fun saveQueue(queue: List<Track>)

    // Favorites
    fun getFavorites(): Flow<List<Track>>
    suspend fun addFavorite(track: Track)
    suspend fun removeFavorite(trackId: String)
    suspend fun isFavorite(trackId: String): Boolean

    // Recent plays
    fun getRecentPlays(): Flow<List<Track>>
    suspend fun recordRecentPlay(track: Track)

    // NEW: Album/Artist recent plays
    fun getRecentAlbums(): Flow<List<Album>>
    fun getRecentArtists(): Flow<List<Artist>>
    suspend fun recordRecentAlbum(album: Album)
    suspend fun recordRecentArtist(artist: Artist)

    // Playlists
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistTracks(playlistId: Long): Flow<List<Track>>
    suspend fun addPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    // Downloads
    val downloadStates: StateFlow<Map<String, com.example.musicplayer.util.DownloadState>>
    fun getDownloadedTracks(): Flow<List<Track>>
    suspend fun isTrackDownloaded(trackId: String): Boolean
    suspend fun downloadTrack(track: Track)
    suspend fun deleteDownload(trackId: String)
}

data class Category(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
    val description: String? = null
)
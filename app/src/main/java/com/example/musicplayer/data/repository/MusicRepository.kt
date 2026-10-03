package com.example.musicplayer.data.repository

import com.example.musicplayer.data.provider.MusicProvider
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

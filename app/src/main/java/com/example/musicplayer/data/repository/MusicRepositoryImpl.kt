package com.example.musicplayer.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.musicplayer.data.local.AlbumDao
import com.example.musicplayer.data.local.ArtistDao
import com.example.musicplayer.data.local.AppDatabase
import com.example.musicplayer.data.local.DownloadedTrackDao
import com.example.musicplayer.data.local.FavoriteDao
import com.example.musicplayer.data.local.FavoriteEntity
import com.example.musicplayer.data.local.PlaylistDao
import com.example.musicplayer.data.local.PlaylistEntity
import com.example.musicplayer.data.local.PlaylistTrackDao
import com.example.musicplayer.data.local.PlaylistTrackEntity
import com.example.musicplayer.data.local.QueueDao
import com.example.musicplayer.data.local.QueueEntity
import com.example.musicplayer.data.local.RecentPlayDao
import com.example.musicplayer.data.local.RecentPlayEntity
import com.example.musicplayer.data.local.TrackDao
import com.example.musicplayer.data.provider.AudiusProvider
import com.example.musicplayer.data.provider.FmaProvider
import com.example.musicplayer.data.provider.JamendoProvider
import com.example.musicplayer.data.provider.JioSaavnProvider
import com.example.musicplayer.data.provider.MusicProvider
import com.example.musicplayer.data.provider.YouTubeMusicProvider
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import com.example.musicplayer.util.AudioDownloadManager
import com.example.musicplayer.util.EncryptedPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val jioSaavnProvider: JioSaavnProvider,
    private val youTubeMusicProvider: YouTubeMusicProvider,
    private val audiusProvider: AudiusProvider,
    private val fmaProvider: FmaProvider,
    private val jamendoProvider: JamendoProvider,
    private val favoriteDao: FavoriteDao,
    private val recentPlayDao: RecentPlayDao,
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
    private val downloadedTrackDao: DownloadedTrackDao,
    private val queueDao: QueueDao,
    private val encryptedPrefs: EncryptedPrefs,
    private val downloadManager: AudioDownloadManager,
    @ApplicationContext private val appContext: android.content.Context
) : MusicRepository {

    private val providers = listOf<MusicProvider>(
        jioSaavnProvider,
        youTubeMusicProvider,
        audiusProvider,
        fmaProvider,
        jamendoProvider
    )

    private val _activeProviderFlow = MutableStateFlow(resolveInitialProvider())
    override val activeProviderFlow: StateFlow<MusicProvider> = _activeProviderFlow.asStateFlow()

    private fun resolveInitialProvider(): MusicProvider {
        val savedId = encryptedPrefs.getActiveProviderId()
        return providers.firstOrNull { it.providerId == savedId } ?: jioSaavnProvider
    }

    override fun getSupportedProviders(): List<MusicProvider> = providers

    override fun setActiveProvider(providerId: String) {
        val selected = providers.firstOrNull { it.providerId == providerId } ?: jioSaavnProvider
        encryptedPrefs.setActiveProviderId(selected.providerId)
        _activeProviderFlow.value = selected
    }

    override fun getActiveProvider(): MusicProvider = _activeProviderFlow.value

    override suspend fun search(query: String): List<Track> = getActiveProvider().search(query)

    override suspend fun getTrending(): List<Track> = getActiveProvider().getTrending()

    override suspend fun getSimilarTracks(track: Track): List<Track> =
        getActiveProvider().getSimilarTracks(track)

    override suspend fun getTrackStreamUrl(track: Track): String {
        // Check if track is downloaded locally on disk
        val localPath = downloadManager.getLocalAudioPath(track.id)
        if (!localPath.isNullOrBlank()) {
            return localPath
        }
        if (!track.streamUrl.isNullOrBlank()) {
            return track.streamUrl
        }
        return getActiveProvider().getTrackStreamUrl(track.id)
    }

    // NEW: Album operations
    override suspend fun getAlbum(albumId: String): Album? = getActiveProvider().getAlbum(albumId)
    override suspend fun getAlbumTracks(albumId: String): List<Track> = getActiveProvider().getAlbumTracks(albumId)
    override suspend fun getArtistAlbums(artistId: String): List<Album> = getActiveProvider().getArtistAlbums(artistId)
    override suspend fun getNewReleases(): List<Album> = getActiveProvider().getNewReleases()
    override suspend fun searchAlbums(query: String): List<Album> = getActiveProvider().searchAlbums(query)

    // NEW: Artist operations
    override suspend fun getArtist(artistId: String): Artist? = getActiveProvider().getArtist(artistId)
    override suspend fun getArtistTopTracks(artistId: String): List<Track> = getActiveProvider().getArtistTopTracks(artistId)
    override suspend fun getSimilarArtists(artistId: String): List<Artist> = getActiveProvider().getSimilarArtists(artistId)
    override suspend fun searchArtists(query: String): List<Artist> = getActiveProvider().searchArtists(query)

    // Queue persistence
    override fun getPersistedQueue(): Flow<List<Track>> =
        queueDao.getQueueFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveQueue(queue: List<Track>) {
        queueDao.clearQueue()
        val entities = queue.mapIndexed { index, track ->
            QueueEntity.fromDomain(track, index)
        }
        queueDao.insertQueue(entities)
    }

    // Favorites
    override fun getFavorites(): Flow<List<Track>> = favoriteDao.getAllFavorites()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun addFavorite(track: Track) {
        favoriteDao.insertFavorite(FavoriteEntity.fromDomain(track))
    }

    override suspend fun removeFavorite(trackId: String) {
        favoriteDao.deleteFavoriteById(trackId)
    }

    override suspend fun isFavorite(trackId: String): Boolean {
        return favoriteDao.isFavorite(trackId)
    }

    // Recent plays
    override fun getRecentPlays(): Flow<List<Track>> = recentPlayDao.getRecentPlays()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun recordRecentPlay(track: Track) {
        val timestamp = System.currentTimeMillis()
        recentPlayDao.insertRecentPlay(RecentPlayEntity.fromDomain(track, timestamp))
    }

    // NEW: Recent albums/artists - using separate DAOs for proper storage
    override fun getRecentAlbums(): Flow<List<Album>> = flowOf(emptyList())

    override fun getRecentArtists(): Flow<List<Artist>> = flowOf(emptyList())

    override suspend fun recordRecentAlbum(album: Album) {
        // TODO: Implement with separate AlbumRecentDao when available
        // For now, we just record the album as a recent play via its tracks
        if (album.trackCount > 0) {
            // Could fetch first track and record that
        }
    }

    override suspend fun recordRecentArtist(artist: Artist) {
        // TODO: Implement with separate ArtistRecentDao when available
        // For now, no-op
    }

    // Playlists
    override fun getPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
        .map { list -> list.map { it.toDomain() } }

    override fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> =
        playlistTrackDao.getTracksForPlaylist(playlistId)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun addPlaylist(name: String): Long {
        val entity = PlaylistEntity(name = name, trackIds = "")
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistTrackDao.deleteAllTracksForPlaylist(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        playlistTrackDao.insertTrack(PlaylistTrackEntity.fromDomain(playlistId, track))
        val playlists = playlistDao.getAllPlaylists().firstOrNull() ?: emptyList()
        val target = playlists.firstOrNull { it.id == playlistId } ?: return
        val existingIds = if (target.trackIds.isBlank()) emptyList() else target.trackIds.split(",")
        if (!existingIds.contains(track.id)) {
            val updated = (existingIds + track.id).joinToString(",")
            playlistDao.updateTrackIds(playlistId, updated)
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        playlistTrackDao.deleteTrack(playlistId, trackId)
        val playlists = playlistDao.getAllPlaylists().firstOrNull() ?: emptyList()
        val target = playlists.firstOrNull { it.id == playlistId } ?: return
        val existingIds = if (target.trackIds.isBlank()) emptyList() else target.trackIds.split(",")
        if (existingIds.contains(trackId)) {
            val updated = existingIds.filter { it != trackId }.joinToString(",")
            playlistDao.updateTrackIds(playlistId, updated)
        }
    }

    // NEW: Browse/Discover
    override suspend fun getFeaturedPlaylists(): List<Playlist> {
        return try {
            val provider = getActiveProvider()
            when (provider) {
                is JioSaavnProvider -> provider.getFeaturedPlaylists()
                is YouTubeMusicProvider -> provider.getFeaturedPlaylists()
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCategories(): List<Category> {
        return try {
            val provider = getActiveProvider()
            when (provider) {
                is JioSaavnProvider -> provider.getCategories()
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCategoryPlaylists(categoryId: String): List<Playlist> {
        return try {
            val provider = getActiveProvider()
            when (provider) {
                is JioSaavnProvider -> provider.getCategoryPlaylists(categoryId)
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getRecommendations(seedTracks: List<String>, seedArtists: List<String>): List<Track> {
        return try {
            val provider = getActiveProvider()
            provider.getRecommendations(seedTracks, seedArtists)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lyrics
    override suspend fun getLyrics(trackId: String): String? {
        return try {
            getActiveProvider().getLyrics(trackId)
        } catch (e: Exception) {
            null
        }
    }

    // Downloads
    override val downloadStates: StateFlow<Map<String, com.example.musicplayer.util.DownloadState>>
        get() = downloadManager.downloadStates

    override fun getDownloadedTracks(): Flow<List<Track>> =
        downloadedTrackDao.getAllDownloadedTracks().map { list -> list.map { it.toDomain() } }

    override suspend fun isTrackDownloaded(trackId: String): Boolean =
        downloadManager.isTrackDownloaded(trackId)

    override suspend fun downloadTrack(track: Track) {
        val streamUrl = getTrackStreamUrl(track)
        if (streamUrl.isNotBlank()) {
            downloadManager.downloadTrack(track, streamUrl)
        }
    }

    override suspend fun deleteDownload(trackId: String) {
        downloadManager.deleteDownload(trackId)
    }
}
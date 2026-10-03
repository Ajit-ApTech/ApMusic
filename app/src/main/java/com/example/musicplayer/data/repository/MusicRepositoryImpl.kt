package com.example.musicplayer.data.repository

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
import com.example.musicplayer.data.provider.AudiusProvider
import com.example.musicplayer.data.provider.FmaProvider
import com.example.musicplayer.data.provider.JamendoProvider
import com.example.musicplayer.data.provider.JioSaavnProvider
import com.example.musicplayer.data.provider.MusicProvider
import com.example.musicplayer.data.provider.YouTubeMusicProvider
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.util.EncryptedPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import com.example.musicplayer.data.local.DownloadedTrackDao
import com.example.musicplayer.util.AudioDownloadManager

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
    private val downloadManager: AudioDownloadManager
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

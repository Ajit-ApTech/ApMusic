package com.example.musicplayer.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import com.example.musicplayer.util.AudioEffectManager
import com.example.musicplayer.util.EqualizerState
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.musicplayer.domain.model.Playlist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    val controllerManager: MusicControllerManager,
    private val repository: MusicRepository,
    val audioEffectManager: AudioEffectManager
) : ViewModel() {

    val eqState: StateFlow<EqualizerState> = audioEffectManager.state

    val currentTrack: StateFlow<Track?> = controllerManager.currentTrack
    val isPlaying: StateFlow<Boolean> = controllerManager.isPlaying
    val positionMs: StateFlow<Long> = controllerManager.playbackPositionMs
    val durationMs: StateFlow<Long> = controllerManager.durationMs
    val queue: StateFlow<List<Track>> = controllerManager.queue
    val isShuffle: StateFlow<Boolean> = controllerManager.isShuffle
    val repeatMode: StateFlow<Int> = controllerManager.repeatMode

    val playlists: StateFlow<List<Playlist>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePlayPause() = controllerManager.togglePlayPause()
    fun skipNext() = controllerManager.skipNext()
    fun skipPrevious() = controllerManager.skipPrevious()
    fun seekTo(positionMs: Long) = controllerManager.seekTo(positionMs)
    fun toggleShuffle() = controllerManager.toggleShuffle()
    fun toggleRepeat() = controllerManager.toggleRepeat()
    fun playQueueIndex(index: Int) = controllerManager.playQueueIndex(index)

    suspend fun isFavorite(trackId: String): Boolean = repository.isFavorite(trackId)

    fun toggleFavorite(track: Track, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            if (currentlyFavorite) {
                repository.removeFavorite(track.id)
            } else {
                repository.addFavorite(track)
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, track)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.addPlaylist(name)
        }
    }

    suspend fun isTrackDownloaded(trackId: String): Boolean = repository.isTrackDownloaded(trackId)

    fun toggleDownload(track: Track, currentlyDownloaded: Boolean) {
        viewModelScope.launch {
            if (currentlyDownloaded) {
                repository.deleteDownload(track.id)
            } else {
                repository.downloadTrack(track)
            }
        }
    }
}

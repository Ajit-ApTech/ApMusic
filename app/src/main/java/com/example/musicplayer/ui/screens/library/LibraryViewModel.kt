package com.example.musicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val controllerManager: MusicControllerManager
) : ViewModel() {

    val favorites: StateFlow<List<Track>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Track>> = repository.getRecentPlays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<Track>> = repository.getDownloadedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0) // 0: Liked, 1: Playlists, 2: History, 3: Downloaded
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val downloadStates = repository.downloadStates

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.downloadTrack(track)
        }
    }

    fun deleteDownload(trackId: String) {
        viewModelScope.launch {
            repository.deleteDownload(trackId)
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun playTrack(track: Track, queue: List<Track>) {
        controllerManager.playTrack(track, queue)
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addPlaylist(name.trim())
        }
    }

    fun getPlaylistTracks(playlistId: Long) = repository.getPlaylistTracks(playlistId)

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }
}

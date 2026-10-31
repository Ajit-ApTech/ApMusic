package com.example.musicplayer.ui.screens.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val controllerManager: MusicControllerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _artist = MutableStateFlow<Artist?>(null)
    val artist: StateFlow<Artist?> = _artist.asStateFlow()

    private val _topTracks = MutableStateFlow<List<Track>>(emptyList())
    val topTracks: StateFlow<List<Track>> = _topTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val downloadStates = repository.downloadStates
    val downloadedTracks = repository.getDownloadedTracks()

    init {
        savedStateHandle.get<String>("artistId")?.let { id ->
            if (id.isNotBlank()) {
                loadArtist(id)
            }
        }
    }

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val artistData = repository.getArtist(artistId)
                _artist.value = artistData
                if (artistData != null) {
                    val tracks = repository.getArtistTopTracks(artistId)
                    _topTracks.value = tracks
                } else {
                    _errorMessage.value = "Artist not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load artist: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playTrack(track: Track, queue: List<Track>) {
        controllerManager.playTrack(track, queue)
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.downloadTrack(track)
        }
    }
}
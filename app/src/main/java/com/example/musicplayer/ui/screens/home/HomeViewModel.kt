package com.example.musicplayer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.provider.MusicProvider
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val controllerManager: MusicControllerManager
) : ViewModel() {

    private val _trending = MutableStateFlow<List<Track>>(emptyList())
    val trending: StateFlow<List<Track>> = _trending.asStateFlow()

    private val _recentPlays = MutableStateFlow<List<Track>>(emptyList())
    val recentPlays: StateFlow<List<Track>> = _recentPlays.asStateFlow()

    private val _newReleases = MutableStateFlow<List<Album>>(emptyList())
    val newReleases: StateFlow<List<Album>> = _newReleases.asStateFlow()

    private val _popularArtists = MutableStateFlow<List<Artist>>(emptyList())
    val popularArtists: StateFlow<List<Artist>> = _popularArtists.asStateFlow()

    private val _featuredPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val featuredPlaylists: StateFlow<List<Playlist>> = _featuredPlaylists.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingBrowse = MutableStateFlow(true)
    val isLoadingBrowse: StateFlow<Boolean> = _isLoadingBrowse.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val activeProvider: StateFlow<MusicProvider> = repository.activeProviderFlow

    val downloadStates = repository.downloadStates
    val downloadedTracks = repository.getDownloadedTracks()

    init {
        observeRecentPlays()
        observeActiveProviderAndLoad()
    }

    private fun observeRecentPlays() {
        viewModelScope.launch {
            repository.getRecentPlays().collectLatest {
                _recentPlays.value = it
            }
        }
    }

    private fun observeActiveProviderAndLoad() {
        viewModelScope.launch {
            repository.activeProviderFlow.collectLatest {
                loadTrending()
                loadBrowseData()
            }
        }
    }

    fun loadTrending() {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val results = repository.getTrending()
                if (results.isEmpty()) {
                    val provider = repository.getActiveProvider()
                    if (provider.requiresApiKey) {
                        _errorMessage.value = "${provider.displayName} returned no tracks. An API Key may be required in Settings."
                    }
                }
                _trending.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load trending tracks: ${e.localizedMessage ?: "Unknown error"}. Check connection or Settings."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBrowseData() {
        viewModelScope.launch {
            _isLoadingBrowse.value = true
            try {
                _newReleases.value = repository.getNewReleases()
            } catch (e: Exception) {
                _newReleases.value = emptyList()
            }
            try {
                _featuredPlaylists.value = repository.getFeaturedPlaylists()
            } catch (e: Exception) {
                _featuredPlaylists.value = emptyList()
            }
            // Popular artists - derive from trending tracks
            try {
                val trending = repository.getTrending()
                val artists = trending.mapNotNull { track ->
                    val artistId = track.artistId
                    if (artistId != null) {
                        Artist(
                            id = artistId,
                            name = track.artist,
                            artworkUrl = track.artworkUrl,
                            popularity = track.popularity
                        )
                    } else null
                }.distinctBy { it.id }.sortedByDescending { it.popularity }.take(10)
                _popularArtists.value = artists
            } catch (e: Exception) {
                _popularArtists.value = emptyList()
            }
            _isLoadingBrowse.value = false
        }
    }

    fun playTrack(track: Track, fromTrending: Boolean = true) {
        val q = if (fromTrending && _trending.value.isNotEmpty()) _trending.value else listOf(track)
        controllerManager.playTrack(track, q)
    }

    fun playAlbum(album: Album) {
        viewModelScope.launch {
            val tracks = repository.getAlbumTracks(album.id)
            if (tracks.isNotEmpty()) {
                controllerManager.playTrack(tracks.first(), tracks)
            }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.downloadTrack(track)
        }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
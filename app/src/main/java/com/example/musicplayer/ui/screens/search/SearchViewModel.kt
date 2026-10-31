package com.example.musicplayer.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_QUERY = "search_query"

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val controllerManager: MusicControllerManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Query persisted via SavedStateHandle so it survives process recreation
    // and overlay/recomposition cycles
    private val _query = MutableStateFlow(savedStateHandle.get<String>(KEY_QUERY) ?: "")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val activeProviderName: String
        get() = repository.getActiveProvider().displayName

    val downloadStates = repository.downloadStates
    val downloadedTracks = repository.getDownloadedTracks()

    init {
        // If we have a persisted query on init, immediately restore results
        val initial = _query.value
        if (initial.isNotBlank()) {
            viewModelScope.launch { performSearch(initial) }
        }

        viewModelScope.launch {
            _query
                .debounce(350L)
                .collectLatest { q ->
                    if (q.isNotBlank()) {
                        performSearch(q)
                    } else {
                        _tracks.value = emptyList()
                        _albums.value = emptyList()
                        _artists.value = emptyList()
                        _isSearching.value = false
                    }
                }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        savedStateHandle[KEY_QUERY] = newQuery   // persist for process recreation
        if (newQuery.isNotBlank()) {
            _isSearching.value = true
        }
    }

    private suspend fun performSearch(q: String) {
        _isSearching.value = true
        try {
            _tracks.value = repository.search(q)
            try {
                _albums.value = repository.searchAlbums(q)
            } catch (e: Exception) {
                _albums.value = emptyList()
            }
            try {
                _artists.value = repository.searchArtists(q)
            } catch (e: Exception) {
                _artists.value = emptyList()
            }
        } catch (e: Exception) {
            _tracks.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            controllerManager.playTrack(track, _tracks.value.ifEmpty { listOf(track) })
            val similar = repository.getSimilarTracks(track)
            val smartQueue = _tracks.value.ifEmpty { listOf(track) } + similar.filter { t ->
                t.id != track.id && _tracks.value.none { it.id == t.id }
            }
            controllerManager.updateQueue(smartQueue)
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.downloadTrack(track)
        }
    }
}

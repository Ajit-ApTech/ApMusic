package com.example.musicplayer.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
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

    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val results: StateFlow<List<Track>> = _results.asStateFlow()

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
                        _results.value = emptyList()
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
            val list = repository.search(q)
            _results.value = list
        } catch (e: Exception) {
            _results.value = emptyList()
        } finally {
            _isSearching.value = false
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            controllerManager.playTrack(track, _results.value.ifEmpty { listOf(track) })
            val similar = repository.getSimilarTracks(track)
            val smartQueue = _results.value.ifEmpty { listOf(track) } + similar.filter { t ->
                t.id != track.id && _results.value.none { it.id == t.id }
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

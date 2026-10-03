package com.example.musicplayer.ui.screens.localfiles

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.service.MusicControllerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LocalFilesState {
    object Loading : LocalFilesState()
    object PermissionRequired : LocalFilesState()
    object Empty : LocalFilesState()
    data class Success(val tracks: List<Track>) : LocalFilesState()
}

enum class LocalSortOrder {
    DATE_ADDED_NEWEST,
    DATE_ADDED_OLDEST,
    TITLE_AZ,
    ARTIST_AZ,
    SIZE_LARGEST
}

@HiltViewModel
class LocalFilesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val controllerManager: MusicControllerManager
) : ViewModel() {

    private val _state = MutableStateFlow<LocalFilesState>(LocalFilesState.Loading)
    val state: StateFlow<LocalFilesState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(LocalSortOrder.DATE_ADDED_NEWEST)
    val sortOrder: StateFlow<LocalSortOrder> = _sortOrder.asStateFlow()

    // Full unfiltered list stored for client-side filtering
    private var allLocalTracks: List<Track> = emptyList()

    init {
        scanDeviceAudio()
    }

    fun scanDeviceAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LocalFilesState.Loading
            try {
                val tracks = queryMediaStore()
                allLocalTracks = tracks
                applyFilterAndSort()
            } catch (e: SecurityException) {
                _state.value = LocalFilesState.PermissionRequired
            } catch (e: Exception) {
                _state.value = LocalFilesState.Empty
            }
        }
    }

    fun onPermissionDenied() {
        _state.value = LocalFilesState.PermissionRequired
    }

    private fun queryMediaStore(): List<Track> {
        val tracks = mutableListOf<Track>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE
        )

        // Only query audio files (mp3, flac, m4a, ogg, wav)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Unknown"
                val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                val albumId = cursor.getLong(albumIdCol)
                val durationMs = cursor.getLong(durationCol).toInt()

                // Content URI for the audio file (ExoPlayer can play this directly)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )

                // Album art URI
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                )

                tracks.add(
                    Track(
                        id = "local_$id",
                        title = title,
                        artist = artist,
                        artworkUrl = albumArtUri.toString(),
                        durationMs = durationMs,
                        streamUrl = contentUri.toString()  // content:// URI for local playback
                    )
                )
            }
        }

        return tracks
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilterAndSort()
    }

    fun onSortOrderChanged(order: LocalSortOrder) {
        _sortOrder.value = order
        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        val query = _searchQuery.value.lowercase()
        val filtered = if (query.isBlank()) {
            allLocalTracks
        } else {
            allLocalTracks.filter {
                it.title.lowercase().contains(query) || it.artist.lowercase().contains(query)
            }
        }

        val sorted = when (_sortOrder.value) {
            LocalSortOrder.DATE_ADDED_NEWEST -> filtered  // MediaStore already sorted DESC
            LocalSortOrder.DATE_ADDED_OLDEST -> filtered.reversed()
            LocalSortOrder.TITLE_AZ -> filtered.sortedBy { it.title.lowercase() }
            LocalSortOrder.ARTIST_AZ -> filtered.sortedBy { it.artist.lowercase() }
            LocalSortOrder.SIZE_LARGEST -> filtered  // Size is not in Track model, keep original order
        }

        _state.value = if (sorted.isEmpty()) LocalFilesState.Empty else LocalFilesState.Success(sorted)
    }

    fun playTrack(track: Track, queue: List<Track>) {
        controllerManager.playTrack(track, queue)
    }

    fun playAll(queue: List<Track>) {
        if (queue.isNotEmpty()) {
            controllerManager.playTrack(queue.first(), queue)
        }
    }

    fun shuffleAll(queue: List<Track>) {
        if (queue.isNotEmpty()) {
            val shuffled = queue.shuffled()
            controllerManager.playTrack(shuffled.first(), shuffled)
        }
    }
}

package com.example.musicplayer.service

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.util.EncryptedPrefs
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicControllerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
    private val encryptedPrefs: EncryptedPrefs
 ) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()
    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    init {
        initializeController()
        startPositionTracker()
        restorePersistedState()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val c = controllerFuture?.get() ?: return@addListener
                controller = c
                attachControllerListener(c)
                updateFromPlayerState(c)
            } catch (e: Exception) {
                // Controller init exception
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun attachControllerListener(player: MediaController) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (!isPlaying) {
                    saveCurrentPlaybackState()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _durationMs.value = player.duration.coerceAtLeast(0L)
                _isPlaying.value = player.isPlaying

                // When song finishes playing
                if (playbackState == Player.STATE_ENDED) {
                    if (_repeatMode.value == Player.REPEAT_MODE_ONE) {
                        player.seekTo(0L)
                        player.play()
                    } else {
                        // Automatically advance to the next song in the queue
                        skipNext()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val trackId = mediaItem?.mediaId
                val matched = _queue.value.firstOrNull { it.id == trackId }
                if (matched != null) {
                    _currentTrack.value = matched
                    scope.launch(Dispatchers.IO) {
                        repository.recordRecentPlay(matched)
                    }
                }
                saveCurrentPlaybackState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _isShuffle.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(mode: Int) {
                _repeatMode.value = mode
            }
        })
    }

    private fun updateFromPlayerState(player: MediaController) {
        _isPlaying.value = player.isPlaying
        _isShuffle.value = player.shuffleModeEnabled
        _repeatMode.value = player.repeatMode
        val trackId = player.currentMediaItem?.mediaId
        _currentTrack.value = _queue.value.firstOrNull { it.id == trackId }
        _durationMs.value = player.duration.coerceAtLeast(0L)
        _playbackPositionMs.value = player.currentPosition.coerceAtLeast(0L)
    }

    private fun startPositionTracker() {
        scope.launch {
            while (isActive) {
                controller?.let { c ->
                    if (c.isPlaying) {
                        _playbackPositionMs.value = c.currentPosition.coerceAtLeast(0L)
                        _durationMs.value = c.duration.coerceAtLeast(0L)
                    }
                }
                delay(500L)
            }
        }
    }

    fun playTrack(track: Track, newQueue: List<Track> = listOf(track)) {
        scope.launch {
            val resolvedQueue = if (newQueue.none { it.id == track.id }) {
                listOf(track) + newQueue
            } else {
                newQueue
            }
            _queue.value = resolvedQueue
            _currentTrack.value = track

            // Persist queue
            repository.saveQueue(resolvedQueue)

            val mediaItems = resolvedQueue.map { buildMediaItem(it) }
            val startIndex = resolvedQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

            controller?.let { c ->
                c.setMediaItems(mediaItems, startIndex, 0L)
                c.prepare()
                c.play()
            }

            scope.launch(Dispatchers.IO) {
                repository.recordRecentPlay(track)
            }
            saveCurrentPlaybackState()

            // If queue is single song (e.g. from search), auto-fetch similar songs by same artist/album
            if (resolvedQueue.size <= 1) {
                fetchAndAppendSimilarTracks(track)
            }
        }
    }

    private fun fetchAndAppendSimilarTracks(track: Track) {
        scope.launch(Dispatchers.IO) {
            try {
                val similar = repository.getSimilarTracks(track)
                if (similar.isNotEmpty()) {
                    val currentQ = _queue.value
                    val existingIds = currentQ.map { it.id }.toSet()
                    val newTracks = similar.filter { it.id !in existingIds }
                    if (newTracks.isNotEmpty()) {
                        val updatedQueue = currentQ + newTracks
                        _queue.value = updatedQueue
                        repository.saveQueue(updatedQueue)

                        val newMediaItems = newTracks.map { buildMediaItem(it) }
                        scope.launch(Dispatchers.Main) {
                            controller?.addMediaItems(newMediaItems)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playQueueIndex(index: Int) {
        val q = _queue.value
        if (index in q.indices) {
            playTrack(q[index], q)
        }
    }

    fun togglePlayPause() {
        controller?.let { c ->
            if (c.isPlaying) c.pause() else c.play()
        }
    }

    fun skipNext() {
        val c = controller
        if (c != null && c.hasNextMediaItem()) {
            c.seekToNextMediaItem()
            c.play()
        } else {
            val q = _queue.value
            if (q.isEmpty()) return
            val currentId = _currentTrack.value?.id
            val currentIndex = q.indexOfFirst { it.id == currentId }

            val nextIndex = if (_isShuffle.value) {
                if (q.size > 1) {
                    val candidates = q.indices.filter { it != currentIndex }
                    candidates.random()
                } else 0
            } else {
                if (currentIndex >= 0 && currentIndex < q.size - 1) {
                    currentIndex + 1
                } else 0
            }

            if (nextIndex in q.indices) {
                playTrack(q[nextIndex], q)
            }
        }
    }

    fun updateQueue(newQueue: List<Track>) {
        if (_currentTrack.value?.id != null) {
            _queue.value = newQueue
            scope.launch(Dispatchers.IO) {
                repository.saveQueue(newQueue)
            }
        }
    }

    fun skipPrevious() {
        val c = controller
        if (c != null) {
            if (c.currentPosition > 3000L) {
                c.seekTo(0L)
                _playbackPositionMs.value = 0L
                return
            }
            if (c.hasPreviousMediaItem()) {
                c.seekToPreviousMediaItem()
                c.play()
                return
            }
        }

        val q = _queue.value
        if (q.isEmpty()) return
        val currentId = _currentTrack.value?.id
        val currentIndex = q.indexOfFirst { it.id == currentId }
        val prevIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            q.lastIndex
        }

        if (prevIndex in q.indices) {
            playTrack(q[prevIndex], q)
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _playbackPositionMs.value = positionMs
        saveCurrentPlaybackState()
    }

    fun toggleShuffle() {
        controller?.let { c ->
            val nextState = !c.shuffleModeEnabled
            c.shuffleModeEnabled = nextState
            _isShuffle.value = nextState
        }
    }

    fun toggleRepeat() {
        controller?.let { c ->
            val nextMode = when (c.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            c.repeatMode = nextMode
            _repeatMode.value = nextMode
        }
    }

    private fun buildMediaItem(track: Track): MediaItem {
        val uri = when {
            track.streamUrl.isNullOrBlank() -> Uri.EMPTY
            // Local content URI (from MediaStore)
            track.streamUrl.startsWith("content://") -> Uri.parse(track.streamUrl)
            // Local file path (from AudioDownloadManager)
            track.streamUrl.startsWith("/") -> Uri.fromFile(java.io.File(track.streamUrl))
            // file:// scheme
            track.streamUrl.startsWith("file://") -> Uri.parse(track.streamUrl)
            // Remote HTTP/HTTPS stream
            else -> Uri.parse(track.streamUrl)
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.artist) // Show artist as album title on lock screen for better display
            .setArtworkUri(track.artworkUrl?.let { Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun saveCurrentPlaybackState() {
        _currentTrack.value?.let { track ->
            encryptedPrefs.setLastTrackId(track.id)
            val pos = controller?.currentPosition ?: _playbackPositionMs.value
            encryptedPrefs.setLastPositionMs(pos)
        }
    }

    private fun restorePersistedState() {
        scope.launch {
            val persisted = repository.getPersistedQueue().firstOrNull() ?: emptyList()
            if (persisted.isNotEmpty()) {
                _queue.value = persisted
                val lastId = encryptedPrefs.getLastTrackId()
                val lastTrack = persisted.firstOrNull { it.id == lastId } ?: persisted.firstOrNull()
                _currentTrack.value = lastTrack
                _playbackPositionMs.value = encryptedPrefs.getLastPositionMs()
            }
        }
    }

    fun startSleepTimer(durationMs: Long) {
        sleepTimerJob?.cancel()
        if (durationMs <= 0L) {
            _sleepTimerRemainingMs.value = null
            return
        }
        sleepTimerJob = scope.launch {
            var remaining = durationMs
            while (remaining > 0L && isActive) {
                _sleepTimerRemainingMs.value = remaining
                delay(1000L)
                remaining -= 1000L
            }
            _sleepTimerRemainingMs.value = null
            controller?.pause()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingMs.value = null
    }

    fun removeQueueItem(index: Int) {
        val q = _queue.value.toMutableList()
        if (index in q.indices) {
            q.removeAt(index)
            _queue.value = q
            scope.launch(Dispatchers.IO) {
                repository.saveQueue(q)
            }
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        controller?.stop()
        _currentTrack.value = null
        scope.launch(Dispatchers.IO) {
            repository.saveQueue(emptyList())
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val q = _queue.value.toMutableList()
        if (fromIndex in q.indices && toIndex in q.indices) {
            val item = q.removeAt(fromIndex)
            q.add(toIndex, item)
            _queue.value = q
            scope.launch(Dispatchers.IO) {
                repository.saveQueue(q)
            }
        }
    }
}

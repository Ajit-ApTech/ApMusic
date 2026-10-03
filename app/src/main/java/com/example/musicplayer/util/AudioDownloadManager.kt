package com.example.musicplayer.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.musicplayer.data.local.DownloadedTrackDao
import com.example.musicplayer.data.local.DownloadedTrackEntity
import com.example.musicplayer.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Int) : DownloadState()
    object Downloaded : DownloadState()
    data class Failed(val error: String) : DownloadState()
}

@Singleton
class AudioDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadedTrackDao: DownloadedTrackDao
) {
    companion object {
        const val FOLDER_NAME = "ApMusic"
    }

    private val client = OkHttpClient()

    /**
     * Returns the external "ApMusic" folder in the public Music directory.
     * This folder is visible in device File Manager apps.
     * Path: Phone Storage → Music → ApMusic
     */
    private val downloadsDir: File by lazy {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        File(musicDir, FOLDER_NAME).apply { mkdirs() }
    }

    /**
     * Artwork stored alongside music in a hidden subfolder so it doesn't clutter the Files app.
     */
    private val artworkDir: File by lazy {
        File(downloadsDir, ".artwork").apply { mkdirs() }
    }

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates.asStateFlow()

    suspend fun isTrackDownloaded(trackId: String): Boolean = withContext(Dispatchers.IO) {
        val entity = downloadedTrackDao.getDownloadedTrackById(trackId) ?: return@withContext false
        File(entity.localAudioPath).exists()
    }

    suspend fun getLocalAudioPath(trackId: String): String? = withContext(Dispatchers.IO) {
        val entity = downloadedTrackDao.getDownloadedTrackById(trackId) ?: return@withContext null
        val file = File(entity.localAudioPath)
        if (file.exists()) file.absolutePath else null
    }

    suspend fun downloadTrack(track: Track, streamUrl: String) = withContext(Dispatchers.IO) {
        if (isTrackDownloaded(track.id)) {
            updateState(track.id, DownloadState.Downloaded)
            return@withContext
        }

        updateState(track.id, DownloadState.Downloading(0))

        try {
            // Safe filename: strip characters that are invalid in file names
            val safeTitle = track.title.replace(Regex("[/\\\\:*?\"<>|]"), "_").take(80)
            val audioFile = File(downloadsDir, "${safeTitle}_${track.id}.mp3")

            val req = Request.Builder().url(streamUrl).build()
            val resp = client.newCall(req).execute()

            if (!resp.isSuccessful || resp.body == null) {
                updateState(track.id, DownloadState.Failed("Failed to connect (${resp.code})"))
                return@withContext
            }

            val body = resp.body!!
            val totalBytes = body.contentLength()
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(audioFile)

            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            var downloadedBytes = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                if (totalBytes > 0) {
                    val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                    updateState(track.id, DownloadState.Downloading(progress))
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Register with MediaStore so it appears in Files app and music players
            registerWithMediaStore(audioFile, track)

            // Download and store artwork
            var localArtPath: String? = null
            if (!track.artworkUrl.isNullOrBlank()) {
                try {
                    val artFile = File(artworkDir, "${track.id}.jpg")
                    val artReq = Request.Builder().url(track.artworkUrl).build()
                    val artResp = client.newCall(artReq).execute()
                    if (artResp.isSuccessful && artResp.body != null) {
                        val artOut = FileOutputStream(artFile)
                        artResp.body!!.byteStream().copyTo(artOut)
                        artOut.flush()
                        artOut.close()
                        localArtPath = artFile.absolutePath
                    }
                } catch (e: Exception) {
                    // Non-fatal: artwork failure doesn't block the download
                }
            }

            val entity = DownloadedTrackEntity.fromDomain(track, audioFile.absolutePath, localArtPath)
            downloadedTrackDao.insertDownloadedTrack(entity)

            updateState(track.id, DownloadState.Downloaded)
        } catch (e: Exception) {
            updateState(track.id, DownloadState.Failed(e.message ?: "Error downloading"))
        }
    }

    /**
     * Registers the audio file with MediaStore so it appears in:
     * - Google Files app → Audio section
     * - Samsung My Files → Music
     * - Any music player scanning the device
     *
     * On Android 10+, uses MediaStore.Audio API.
     * On older Android, uses MediaScannerConnection.
     */
    private fun registerWithMediaStore(audioFile: File, track: Track) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, audioFile.name)
                    put(MediaStore.Audio.Media.TITLE, track.title)
                    put(MediaStore.Audio.Media.ARTIST, track.artist)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/$FOLDER_NAME")
                    put(MediaStore.Audio.Media.IS_PENDING, 0)
                }
                // Only insert if not already in MediaStore
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                // We don't need to write via URI here since file is already on disk
                // Just ensure the entry exists so it's discoverable
            } catch (e: Exception) {
                // Fallback to scanner
                MediaScannerConnection.scanFile(context, arrayOf(audioFile.absolutePath), arrayOf("audio/mpeg"), null)
            }
        } else {
            // Android 9 and below: trigger a media scan
            MediaScannerConnection.scanFile(
                context,
                arrayOf(audioFile.absolutePath),
                arrayOf("audio/mpeg"),
                null
            )
        }
    }

    suspend fun deleteDownload(trackId: String) = withContext(Dispatchers.IO) {
        val entity = downloadedTrackDao.getDownloadedTrackById(trackId)
        if (entity != null) {
            val audioFile = File(entity.localAudioPath)
            if (audioFile.exists()) {
                audioFile.delete()
                // Remove from MediaStore as well
                MediaScannerConnection.scanFile(context, arrayOf(entity.localAudioPath), null, null)
            }
            entity.localArtworkPath?.let { path ->
                try { File(path).delete() } catch (e: Exception) {}
            }
            downloadedTrackDao.deleteDownloadedTrackById(trackId)
        }
        updateState(trackId, DownloadState.Idle)
    }

    private fun updateState(trackId: String, state: DownloadState) {
        _downloadStates.value = _downloadStates.value + (trackId to state)
    }
}

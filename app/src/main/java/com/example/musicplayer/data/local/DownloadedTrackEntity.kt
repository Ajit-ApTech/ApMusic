package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayer.domain.model.Track

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val localAudioPath: String,
    val localArtworkPath: String?,
    val durationMs: Int,
    val downloadedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            artworkUrl = localArtworkPath ?: artworkUrl,
            durationMs = durationMs,
            streamUrl = localAudioPath
        )
    }

    companion object {
        fun fromDomain(track: Track, audioPath: String, artworkPath: String?): DownloadedTrackEntity {
            return DownloadedTrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                artworkUrl = track.artworkUrl,
                localAudioPath = audioPath,
                localArtworkPath = artworkPath,
                durationMs = track.durationMs
            )
        }
    }
}

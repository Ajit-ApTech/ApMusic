package com.example.musicplayer.data.local

import androidx.room.Entity
import com.example.musicplayer.domain.model.Track

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val durationMs: Int,
    val streamUrl: String?,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Track = Track(
        id = trackId,
        title = title,
        artist = artist,
        artworkUrl = artworkUrl,
        durationMs = durationMs,
        streamUrl = streamUrl
    )

    companion object {
        fun fromDomain(playlistId: Long, track: Track): PlaylistTrackEntity = PlaylistTrackEntity(
            playlistId = playlistId,
            trackId = track.id,
            title = track.title,
            artist = track.artist,
            artworkUrl = track.artworkUrl,
            durationMs = track.durationMs,
            streamUrl = track.streamUrl
        )
    }
}

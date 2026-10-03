package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayer.domain.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val durationMs: Int,
    val streamUrl: String?
) {
    fun toDomain(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        artworkUrl = artworkUrl,
        durationMs = durationMs,
        streamUrl = streamUrl
    )

    companion object {
        fun fromDomain(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            artworkUrl = track.artworkUrl,
            durationMs = track.durationMs,
            streamUrl = track.streamUrl
        )
    }
}

package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayer.domain.model.Track

@Entity(tableName = "recent_plays")
data class RecentPlayEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val playedAt: Long, // epoch millis
    val streamUrl: String? = null
) {
    fun toDomain(): Track = Track(
        id = trackId,
        title = title,
        artist = artist,
        artworkUrl = artworkUrl,
        durationMs = 0,
        streamUrl = streamUrl
    )

    companion object {
        fun fromDomain(track: Track, timestamp: Long): RecentPlayEntity = RecentPlayEntity(
            trackId = track.id,
            title = track.title,
            artist = track.artist,
            artworkUrl = track.artworkUrl,
            playedAt = timestamp,
            streamUrl = track.streamUrl
        )
    }
}

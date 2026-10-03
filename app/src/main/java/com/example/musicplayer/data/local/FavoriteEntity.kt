package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val trackId: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val durationMs: Int,
    val streamUrl: String? = null
) {
    fun toDomain(): com.example.musicplayer.domain.model.Track = com.example.musicplayer.domain.model.Track(
        id = trackId,
        title = title,
        artist = artist,
        artworkUrl = artworkUrl,
        durationMs = durationMs,
        streamUrl = streamUrl
    )

    companion object {
        fun fromDomain(track: com.example.musicplayer.domain.model.Track): FavoriteEntity = FavoriteEntity(
            trackId = track.id,
            title = track.title,
            artist = track.artist,
            artworkUrl = track.artworkUrl,
            durationMs = track.durationMs,
            streamUrl = track.streamUrl
        )
    }
}

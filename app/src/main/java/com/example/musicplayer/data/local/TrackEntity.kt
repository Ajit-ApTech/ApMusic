package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayer.domain.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val albumId: String? = null,
    val albumName: String? = null,
    val albumArtworkUrl: String? = null,
    val artworkUrl: String?,
    val durationMs: Int,
    val streamUrl: String? = null,
    val trackNumber: Int = 0,
    val discNumber: Int = 1,
    val genre: String? = null,
    val year: Int? = null,
    val lyrics: String? = null,
    val explicit: Boolean = false,
    val popularity: Int = 0
) {
    fun toDomain(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        albumId = albumId,
        albumName = albumName,
        albumArtworkUrl = albumArtworkUrl,
        artworkUrl = artworkUrl,
        durationMs = durationMs,
        streamUrl = streamUrl,
        trackNumber = trackNumber,
        discNumber = discNumber,
        genre = genre,
        year = year,
        lyrics = lyrics,
        explicit = explicit,
        popularity = popularity
    )

    companion object {
        fun fromDomain(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            artistId = track.artistId,
            albumId = track.albumId,
            albumName = track.albumName,
            albumArtworkUrl = track.albumArtworkUrl,
            artworkUrl = track.artworkUrl,
            durationMs = track.durationMs,
            streamUrl = track.streamUrl,
            trackNumber = track.trackNumber,
            discNumber = track.discNumber,
            genre = track.genre,
            year = track.year,
            lyrics = track.lyrics,
            explicit = track.explicit,
            popularity = track.popularity
        )
    }
}
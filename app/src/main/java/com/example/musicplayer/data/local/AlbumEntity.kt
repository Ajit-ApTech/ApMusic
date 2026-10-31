package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayer.domain.model.Album

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artist: String,
    val artistId: String? = null,
    val artworkUrl: String? = null,
    val trackCount: Int = 0,
    val durationMs: Int = 0,
    val releaseDate: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val type: String = "ALBUM",
    val explicit: Boolean = false,
    val popularity: Int = 0,
    val externalUrlsJson: String = "{}",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomain(): Album = Album(
        id = id,
        name = name,
        artist = artist,
        artistId = artistId,
        artworkUrl = artworkUrl,
        trackCount = trackCount,
        durationMs = durationMs,
        releaseDate = releaseDate,
        genre = genre,
        year = year,
        type = try {
            Album.AlbumType.valueOf(type.uppercase())
        } catch (e: Exception) {
            Album.AlbumType.ALBUM
        },
        explicit = explicit,
        popularity = popularity,
        externalUrls = com.google.gson.Gson().fromJson(externalUrlsJson, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type)
    )

    companion object {
        fun fromDomain(album: Album): AlbumEntity = AlbumEntity(
            id = album.id,
            name = album.name,
            artist = album.artist,
            artistId = album.artistId,
            artworkUrl = album.artworkUrl,
            trackCount = album.trackCount,
            durationMs = album.durationMs,
            releaseDate = album.releaseDate,
            genre = album.genre,
            year = album.year,
            type = album.type.name,
            explicit = album.explicit,
            popularity = album.popularity,
            externalUrlsJson = com.google.gson.Gson().toJson(album.externalUrls)
        )
    }
}
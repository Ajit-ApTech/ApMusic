package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.musicplayer.domain.model.Artist

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artworkUrl: String? = null,
    val followerCount: Long? = null,
    val bio: String? = null,
    val genresJson: String = "[]",
    val popularity: Int = 0,
    val externalUrlsJson: String = "{}",
    val verified: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomain(): Artist = Artist(
        id = id,
        name = name,
        artworkUrl = artworkUrl,
        followerCount = followerCount,
        bio = bio,
        genres = com.google.gson.Gson().fromJson(genresJson, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type),
        popularity = popularity,
        externalUrls = com.google.gson.Gson().fromJson(externalUrlsJson, object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type),
        verified = verified
    )

    companion object {
        fun fromDomain(artist: Artist): ArtistEntity = ArtistEntity(
            id = artist.id,
            name = artist.name,
            artworkUrl = artist.artworkUrl,
            followerCount = artist.followerCount,
            bio = artist.bio,
            genresJson = com.google.gson.Gson().toJson(artist.genres),
            popularity = artist.popularity,
            externalUrlsJson = com.google.gson.Gson().toJson(artist.externalUrls),
            verified = artist.verified
        )
    }
}
package com.example.musicplayer.domain.model

import androidx.annotation.Keep

@Keep
data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val artistId: String? = null,
    val artworkUrl: String? = null,
    val trackCount: Int = 0,
    val durationMs: Int = 0,
    val releaseDate: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val type: AlbumType = AlbumType.ALBUM,
    val explicit: Boolean = false,
    val popularity: Int = 0,
    val externalUrls: Map<String, String> = emptyMap()
) {
    enum class AlbumType {
        ALBUM, SINGLE, EP, COMPILATION, SOUNDTRACK
    }

    val displayArtist: String = artist
    val displayArtwork: String? = artworkUrl
    val formattedDuration: String = formatDuration(durationMs.toLong())
    val formattedReleaseDate: String = releaseDate?.take(4) ?: year?.toString() ?: "Unknown"
    val hasTracks: Boolean = trackCount > 0

    companion object {
        private fun formatDuration(ms: Long): String {
            val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, totalSeconds % 60) else "%d:%02d".format(minutes, totalSeconds % 60)
        }
    }
}
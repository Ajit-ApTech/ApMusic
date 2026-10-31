package com.example.musicplayer.domain.model

import androidx.annotation.Keep

@Keep
data class Track(
    val id: String,
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
    // Convenience getters for backward compatibility and UI
    val displayArtist: String = artist
    val displayAlbum: String = albumName ?: "Unknown Album"
    val displayArtwork: String? = albumArtworkUrl ?: artworkUrl
    val hasAlbum: Boolean = !albumId.isNullOrBlank()
    val hasArtist: Boolean = !artistId.isNullOrBlank()
    val formattedDuration: String = formatDuration(durationMs.toLong())

    companion object {
        private fun formatDuration(ms: Long): String {
            val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
    }
}
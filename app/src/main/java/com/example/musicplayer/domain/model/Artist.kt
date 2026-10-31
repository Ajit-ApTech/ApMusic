package com.example.musicplayer.domain.model

import androidx.annotation.Keep

@Keep
data class Artist(
    val id: String,
    val name: String,
    val artworkUrl: String? = null,
    val followerCount: Long? = null,
    val bio: String? = null,
    val genres: List<String> = emptyList(),
    val popularity: Int = 0,
    val externalUrls: Map<String, String> = emptyMap(),
    val verified: Boolean = false
) {
    val displayName: String = name
    val displayArtwork: String? = artworkUrl
    val formattedFollowers: String = followerCount?.let { formatCount(it) } ?: "Unknown"
    val primaryGenre: String = genres.firstOrNull() ?: "Music"
    val hasBio: Boolean = !bio.isNullOrBlank()

    companion object {
        private fun formatCount(count: Long): String {
            return when {
                count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
                count >= 1_000 -> "%.1fK".format(count / 1_000.0)
                else -> count.toString()
            }
        }
    }
}
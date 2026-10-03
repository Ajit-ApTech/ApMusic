package com.example.musicplayer.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val durationMs: Int,
    val streamUrl: String? = null
)

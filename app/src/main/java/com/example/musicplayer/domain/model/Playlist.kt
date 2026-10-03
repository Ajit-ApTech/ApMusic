package com.example.musicplayer.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val trackIds: List<String>
)

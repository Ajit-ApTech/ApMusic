package com.example.musicplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Simple playlist representation. For a lightweight implementation we store the list of track IDs
 * as a comma‑separated string. In a production app you would likely model a many‑to‑many
 * relationship with a cross‑ref table, but this keeps the example compact.
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /**
     * Track IDs belonging to this playlist, stored as a comma‑separated string.
     * Empty string means no tracks.
     */
    val trackIds: String = ""
) {
    fun toDomain(): com.example.musicplayer.domain.model.Playlist =
        com.example.musicplayer.domain.model.Playlist(
            id = id,
            name = name,
            trackIds = if (trackIds.isBlank()) emptyList() else trackIds.split(",")
        )

    companion object {
        fun fromDomain(playlist: com.example.musicplayer.domain.model.Playlist): PlaylistEntity =
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                trackIds = playlist.trackIds.joinToString(",")
            )
    }
}

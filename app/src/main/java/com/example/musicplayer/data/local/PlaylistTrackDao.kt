package com.example.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackDao {
    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY addedAt ASC")
    fun getTracksForPlaylist(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deleteTrack(playlistId: Long, trackId: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun deleteAllTracksForPlaylist(playlistId: Long)
}

package com.example.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrackById(id: String): Flow<TrackEntity?>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackByIdSync(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE albumId = :albumId ORDER BY discNumber ASC, trackNumber ASC")
    fun getTracksByAlbum(albumId: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE albumId = :albumId ORDER BY discNumber ASC, trackNumber ASC")
    suspend fun getTracksByAlbumSync(albumId: String): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE artistId = :artistId ORDER BY popularity DESC, title ASC")
    fun getTracksByArtist(artistId: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artistId = :artistId ORDER BY popularity DESC, title ASC")
    suspend fun getTracksByArtistSync(artistId: String): List<TrackEntity>

    @Query("SELECT * FROM tracks ORDER BY popularity DESC LIMIT :limit")
    fun getPopularTracks(limit: Int): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE title LIKE :query OR artist LIKE :query ORDER BY popularity DESC LIMIT 50")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Transaction
    suspend fun upsertTrack(track: TrackEntity) {
        insertTrack(track)
    }
}
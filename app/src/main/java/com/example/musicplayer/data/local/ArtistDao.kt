package com.example.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Query("SELECT * FROM artists WHERE id = :id")
    fun getArtistById(id: String): Flow<ArtistEntity?>

    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getArtistByIdSync(id: String): ArtistEntity?

    @Query("SELECT * FROM artists ORDER BY popularity DESC, name ASC LIMIT :limit")
    fun getTopArtists(limit: Int): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE name LIKE :query ORDER BY popularity DESC LIMIT 50")
    fun searchArtists(query: String): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists ORDER BY lastUpdated DESC LIMIT :limit")
    fun getRecentArtists(limit: Int): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("DELETE FROM artists WHERE id = :id")
    suspend fun deleteArtist(id: String)

    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()

    @Transaction
    suspend fun upsertArtist(artist: ArtistEntity) {
        insertArtist(artist)
    }
}
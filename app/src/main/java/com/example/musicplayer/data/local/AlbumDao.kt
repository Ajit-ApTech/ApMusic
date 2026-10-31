package com.example.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumById(id: String): Flow<AlbumEntity?>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumByIdSync(id: String): AlbumEntity?

    @Query("SELECT * FROM albums WHERE artistId = :artistId ORDER BY year DESC, name ASC")
    fun getAlbumsByArtist(artistId: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE artistId = :artistId ORDER BY year DESC, name ASC")
    suspend fun getAlbumsByArtistSync(artistId: String): List<AlbumEntity>

    @Query("SELECT * FROM albums ORDER BY lastUpdated DESC LIMIT :limit")
    fun getRecentAlbums(limit: Int): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY popularity DESC LIMIT :limit")
    fun getPopularAlbums(limit: Int): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE name LIKE :query OR artist LIKE :query ORDER BY popularity DESC LIMIT 50")
    fun searchAlbums(query: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbum(id: String)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()

    @Transaction
    suspend fun upsertAlbum(album: AlbumEntity) {
        insertAlbum(album)
    }
}
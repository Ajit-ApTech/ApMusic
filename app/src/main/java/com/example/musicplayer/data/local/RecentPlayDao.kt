package com.example.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayDao {
    @Query("SELECT * FROM recent_plays ORDER BY playedAt DESC")
    fun getRecentPlays(): Flow<List<RecentPlayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentPlay(entity: RecentPlayEntity)
}

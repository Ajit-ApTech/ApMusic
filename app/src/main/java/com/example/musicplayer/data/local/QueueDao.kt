package com.example.musicplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM playback_queue ORDER BY orderIndex ASC")
    fun getQueueFlow(): Flow<List<QueueEntity>>

    @Query("SELECT * FROM playback_queue ORDER BY orderIndex ASC")
    suspend fun getQueue(): List<QueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(queue: List<QueueEntity>)

    @Query("DELETE FROM playback_queue")
    suspend fun clearQueue()
}

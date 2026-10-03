package com.example.musicplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackEntity::class,
        FavoriteEntity::class,
        RecentPlayEntity::class,
        PlaylistEntity::class,
        QueueEntity::class,
        PlaylistTrackEntity::class,
        DownloadedTrackEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentPlayDao(): RecentPlayDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun queueDao(): QueueDao
    abstract fun playlistTrackDao(): PlaylistTrackDao
    abstract fun downloadedTrackDao(): DownloadedTrackDao
}

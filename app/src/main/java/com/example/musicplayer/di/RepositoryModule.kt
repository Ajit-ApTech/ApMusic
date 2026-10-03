package com.example.musicplayer.di

import android.content.Context
import androidx.room.Room
import com.example.musicplayer.data.local.AppDatabase
import com.example.musicplayer.data.local.FavoriteDao
import com.example.musicplayer.data.local.PlaylistDao
import com.example.musicplayer.data.local.QueueDao
import com.example.musicplayer.data.local.RecentPlayDao
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.data.repository.MusicRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.example.musicplayer.data.local.DownloadedTrackDao
import com.example.musicplayer.data.local.PlaylistTrackDao

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {
    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "musicplayer-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideRecentPlayDao(db: AppDatabase): RecentPlayDao = db.recentPlayDao()

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideQueueDao(db: AppDatabase): QueueDao = db.queueDao()

    @Provides
    fun providePlaylistTrackDao(db: AppDatabase): PlaylistTrackDao = db.playlistTrackDao()

    @Provides
    fun provideDownloadedTrackDao(db: AppDatabase): DownloadedTrackDao = db.downloadedTrackDao()
}

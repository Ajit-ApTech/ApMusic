package com.example.musicplayer.di

import android.content.Context
import com.example.musicplayer.util.EncryptedPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): EncryptedPrefs = EncryptedPrefs(context)
}

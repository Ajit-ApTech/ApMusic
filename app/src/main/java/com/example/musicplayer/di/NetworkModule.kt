package com.example.musicplayer.di

import com.example.musicplayer.data.provider.AudiusApi
import com.example.musicplayer.data.provider.FmaApi
import com.example.musicplayer.data.provider.JamendoApi
import com.example.musicplayer.data.provider.JioSaavnApi
import com.example.musicplayer.data.provider.YouTubeMusicApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideJioSaavnApi(client: OkHttpClient, moshi: Moshi): JioSaavnApi {
        return Retrofit.Builder()
            .baseUrl("https://www.jiosaavn.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JioSaavnApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYouTubeMusicApi(client: OkHttpClient, moshi: Moshi): YouTubeMusicApi {
        return Retrofit.Builder()
            .baseUrl("https://yewtu.be/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeMusicApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAudiusApi(client: OkHttpClient, moshi: Moshi): AudiusApi {
        return Retrofit.Builder()
            .baseUrl("https://discoveryprovider.audius.co/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AudiusApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJamendoApi(client: OkHttpClient, moshi: Moshi): JamendoApi {
        return Retrofit.Builder()
            .baseUrl("https://api.jamendo.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JamendoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFmaApi(client: OkHttpClient, moshi: Moshi): FmaApi {
        return Retrofit.Builder()
            .baseUrl("https://archive.org/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FmaApi::class.java)
    }
}

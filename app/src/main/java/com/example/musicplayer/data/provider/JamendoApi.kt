package com.example.musicplayer.data.provider

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApi {
    @GET("v3.0/tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 25,
        @Query("order") order: String = "popularity_total",
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse

    @GET("v3.0/tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("namesearch") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 25,
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse
}

data class JamendoResponse(
    @Json(name = "headers") val headers: JamendoHeaders? = null,
    @Json(name = "results") val results: List<JamendoTrack> = emptyList()
)

data class JamendoHeaders(
    @Json(name = "status") val status: String? = null,
    @Json(name = "code") val code: Int? = null,
    @Json(name = "error_message") val errorMessage: String? = null
)

data class JamendoTrack(
    val id: String,
    val name: String,
    val duration: Int = 0,
    @Json(name = "artist_name") val artistName: String,
    val image: String? = null,
    val audio: String? = null
)

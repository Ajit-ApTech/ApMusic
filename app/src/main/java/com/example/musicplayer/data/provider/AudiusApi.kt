package com.example.musicplayer.data.provider

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AudiusApi {
    @GET("v1/tracks/trending")
    suspend fun getTrending(
        @Query("app_name") appName: String = "MusicPlayerApp",
        @Query("limit") limit: Int = 25
    ): AudiusTracksResponse

    @GET("v1/tracks/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("app_name") appName: String = "MusicPlayerApp",
        @Query("limit") limit: Int = 25
    ): AudiusTracksResponse

    @GET("v1/tracks/{trackId}")
    suspend fun getTrack(
        @Path("trackId") trackId: String,
        @Query("app_name") appName: String = "MusicPlayerApp"
    ): AudiusSingleTrackResponse
}

data class AudiusTracksResponse(
    @Json(name = "data") val data: List<AudiusTrack> = emptyList()
)

data class AudiusSingleTrackResponse(
    @Json(name = "data") val data: AudiusTrack
)

data class AudiusTrack(
    val id: String,
    val title: String,
    val user: AudiusUser,
    val artwork: Map<String, Any?>? = null,
    val duration: Int = 0
)

data class AudiusUser(
    val name: String,
    val handle: String? = null
)

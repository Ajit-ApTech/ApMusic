package com.example.musicplayer.data.provider

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface JioSaavnApi {
    @GET("api.php?__call=search.getResults&_format=json&_marker=0&cc=in&n=25&p=1")
    suspend fun searchSongs(
        @Query("q") query: String
    ): JioSaavnSearchResponse

    @GET("api.php?__call=song.getDetails&_format=json&_marker=0&ctx=web6dot0")
    suspend fun getSongDetails(
        @Query("pids") pids: String
    ): Map<String, Any>
}

data class JioSaavnSearchResponse(
    @Json(name = "total") val total: Any? = null,
    @Json(name = "results") val results: List<JioSaavnSong> = emptyList()
)

data class JioSaavnSong(
    @Json(name = "id") val id: String,
    @Json(name = "song") val song: String = "",
    @Json(name = "primary_artists") val primaryArtists: String = "",
    @Json(name = "singers") val singers: String = "",
    @Json(name = "image") val image: String? = null,
    @Json(name = "duration") val duration: String = "0",
    @Json(name = "encrypted_media_url") val encryptedMediaUrl: String? = null,
    @Json(name = "media_preview_url") val mediaPreviewUrl: String? = null
)

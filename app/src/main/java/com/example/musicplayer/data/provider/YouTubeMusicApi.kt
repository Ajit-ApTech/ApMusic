package com.example.musicplayer.data.provider

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface YouTubeMusicApi {
    @GET
    suspend fun search(
        @Url url: String,
        @Query("q") query: String,
        @Query("type") type: String = "video"
    ): List<YouTubeVideoItem>
}

data class YouTubeVideoItem(
    @Json(name = "videoId") val videoId: String,
    @Json(name = "title") val title: String = "",
    @Json(name = "author") val author: String = "",
    @Json(name = "lengthSeconds") val lengthSeconds: Int = 180,
    @Json(name = "videoThumbnails") val videoThumbnails: List<YouTubeThumbnail> = emptyList()
)

data class YouTubeThumbnail(
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "url") val url: String? = null
)

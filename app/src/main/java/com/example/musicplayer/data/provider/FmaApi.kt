package com.example.musicplayer.data.provider

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface FmaApi {
    @GET("advancedsearch.php")
    suspend fun searchFma(
        @Query("q") q: String,
        @Query("output") output: String = "json",
        @Query("rows") rows: Int = 25
    ): ArchiveSearchResponse
}

data class ArchiveSearchResponse(
    @Json(name = "response") val response: ArchiveResponseData? = null
)

data class ArchiveResponseData(
    @Json(name = "numFound") val numFound: Int = 0,
    @Json(name = "docs") val docs: List<ArchiveDoc> = emptyList()
)

data class ArchiveDoc(
    val identifier: String,
    val title: Any? = null, // Can be String or List<String> in Solr
    val creator: Any? = null
) {
    fun getResolvedTitle(): String {
        return when (title) {
            is String -> title
            is List<*> -> title.firstOrNull()?.toString() ?: "Untitled Track"
            else -> "Untitled Track"
        }
    }

    fun getResolvedCreator(): String {
        return when (creator) {
            is String -> creator
            is List<*> -> creator.firstOrNull()?.toString() ?: "Free Music Archive"
            else -> "Free Music Archive"
        }
    }
}

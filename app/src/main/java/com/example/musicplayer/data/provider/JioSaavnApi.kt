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

    // Album endpoints
    @GET("api.php?__call=content.getAlbumDetails&_format=json&_marker=0&ctx=web6dot0")
    suspend fun getAlbumDetails(
        @Query("albumid") albumId: String
    ): JioSaavnAlbumDetailsResponse

    // Artist endpoints
    @GET("api.php?__call=content.getArtistDetails&_format=json&_marker=0&ctx=web6dot0")
    suspend fun getArtistDetails(
        @Query("artistid") artistId: String
    ): JioSaavnArtistResponse

    @GET("api.php?__call=content.getArtistTopSongs&_format=json&_marker=0&ctx=web6dot0")
    suspend fun getArtistTopSongs(
        @Query("artistid") artistId: String,
        @Query("n") n: Int = 50
    ): JioSaavnArtistSongsResponse

    // Browse endpoints
    @GET("api.php?__call=webapi.get&token=home_new_releases&type=album&n=20&p=1&_format=json&_marker=0")
    suspend fun getNewReleases(): JioSaavnNewReleasesResponse

    @GET("api.php?__call=webapi.get&token=home_featured_playlists&type=playlist&n=20&p=1&_format=json&_marker=0")
    suspend fun getFeaturedPlaylists(): JioSaavnFeaturedPlaylistsResponse

    // Lyrics endpoint
    @GET("api.php?__call=lyrics.get&_format=json&_marker=0&ctx=web6dot0")
    suspend fun getLyrics(
        @Query("trackid") trackId: String
    ): JioSaavnLyricsResponse
}

data class JioSaavnSearchResponse(
    @Json(name = "total") val total: Any? = null,
    @Json(name = "results") val results: List<JioSaavnSong> = emptyList()
)

data class JioSaavnSong(
    @Json(name = "id") val id: String,
    @Json(name = "song") val song: String = "",
    @Json(name = "title") val title: String? = null,
    @Json(name = "primary_artists") val primaryArtists: String = "",
    @Json(name = "singers") val singers: String = "",
    @Json(name = "image") val image: String? = null,
    @Json(name = "duration") val duration: String = "0",
    @Json(name = "encrypted_media_url") val encryptedMediaUrl: String? = null,
    @Json(name = "media_preview_url") val mediaPreviewUrl: String? = null,
    @Json(name = "album") val album: String? = null,
    @Json(name = "album_id") val albumId: String? = null,
    @Json(name = "artist_id") val artistId: String? = null,
    @Json(name = "year") val year: String? = null,
    @Json(name = "track_number") val trackNumber: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "explicit_content") val explicitContent: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "popularity") val popularity: String? = null,
    @Json(name = "more_info") val moreInfo: JioSaavnSongMoreInfo? = null
)

data class JioSaavnSongMoreInfo(
    @Json(name = "encrypted_media_url") val encryptedMediaUrl: String? = null,
    @Json(name = "album_id") val albumId: String? = null,
    @Json(name = "duration") val duration: String? = null
)

data class JioSaavnAlbumDetailsResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "albumid") val albumId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "primary_artists") val primaryArtists: String? = null,
    @Json(name = "singers") val singers: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "song_count") val songCount: String? = null,
    @Json(name = "list_count") val listCount: String? = null,
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "year") val year: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "explicit_content") val explicitContent: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "list") val list: List<JioSaavnSong>? = null,
    @Json(name = "songs") val songs: List<JioSaavnSong>? = null,
    @Json(name = "album") val album: JioSaavnAlbum? = null
)

data class JioSaavnAlbum(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String = "",
    @Json(name = "title") val title: String? = null,
    @Json(name = "primary_artists") val primaryArtists: String = "",
    @Json(name = "image") val image: String? = null,
    @Json(name = "song_count") val songCount: String = "0",
    @Json(name = "duration") val duration: String = "0",
    @Json(name = "year") val year: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "explicit_content") val explicitContent: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "genre") val genre: String? = null
)

data class JioSaavnArtistResponse(
    @Json(name = "artist") val artist: JioSaavnArtist? = null,
    @Json(name = "artistId") val artistId: String? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "follower_count") val followerCount: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "verified") val verified: String? = null,
    @Json(name = "topSongs") val topSongs: List<JioSaavnSong>? = null
)

data class JioSaavnArtist(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String = "",
    @Json(name = "image") val image: String? = null,
    @Json(name = "follower_count") val followerCount: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "verified") val verified: String? = null
)

data class JioSaavnArtistSongsResponse(
    @Json(name = "songs") val songs: List<JioSaavnSong> = emptyList()
)

data class JioSaavnNewReleasesResponse(
    @Json(name = "albums") val albums: List<JioSaavnAlbum> = emptyList()
)

data class JioSaavnFeaturedPlaylistsResponse(
    @Json(name = "playlists") val playlists: List<JioSaavnPlaylist> = emptyList()
)

data class JioSaavnPlaylist(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String = "",
    @Json(name = "image") val image: String? = null,
    @Json(name = "song_count") val songCount: String = "0"
)

data class JioSaavnLyricsResponse(
    @Json(name = "lyrics") val lyrics: String? = null
)
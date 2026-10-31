package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.data.repository.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JioSaavnProvider @Inject constructor(
    private val api: JioSaavnApi
) : MusicProvider {

    override val providerId: String = "jiosaavn"
    override val displayName: String = "JioSaavn (Bollywood & Indian)"
    override val requiresApiKey: Boolean = false

    override suspend fun search(query: String): List<Track> {
        val q = query.ifBlank { "hindi trending" }
        return try {
            val response = api.searchSongs(q)
            response.results.mapNotNull { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrending(): List<Track> {
        return try {
            val response = api.searchSongs("hindi trending songs 2026")
            response.results.mapNotNull { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTrackStreamUrl(trackId: String): String {
        val cleanId = trackId.removePrefix("jiosaavn_")
        return try {
            val searchRes = api.searchSongs(cleanId)
            val matched = searchRes.results.firstOrNull { it.id == cleanId }
            if (matched != null) {
                JioSaavnUtils.decryptMediaUrl(matched.encryptedMediaUrl) ?: ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun getSimilarTracks(track: Track): List<Track> {
        val primaryArtist = track.artist.split(",").first().trim().ifBlank { "hindi hits" }
        return try {
            val response = api.searchSongs(primaryArtist)
            response.results
                .mapNotNull { it.toDomain() }
                .filter { it.id != track.id }
                .take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun testConnection(apiKey: String?): Boolean {
        return try {
            val response = api.searchSongs("arijit")
            response.results.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // In-memory cache for the last fetched album details to prevent redundant back-to-back network calls
    private var lastAlbumDetailsCache: Pair<String, JioSaavnAlbumDetailsResponse>? = null

    private suspend fun fetchAlbumDetails(cleanAlbumId: String): JioSaavnAlbumDetailsResponse? {
        val cached = lastAlbumDetailsCache
        if (cached != null && cached.first == cleanAlbumId) {
            return cached.second
        }
        return try {
            val response = api.getAlbumDetails(cleanAlbumId)
            lastAlbumDetailsCache = Pair(cleanAlbumId, response)
            response
        } catch (e: Exception) {
            null
        }
    }

    // NEW: Album operations
    override suspend fun getAlbum(albumId: String): Album? {
        val cleanId = albumId.removePrefix("jiosaavn_")
        val details = fetchAlbumDetails(cleanId) ?: return null
        return details.toDomain(cleanId)
    }

    override suspend fun getAlbumTracks(albumId: String): List<Track> {
        val cleanId = albumId.removePrefix("jiosaavn_")
        val details = fetchAlbumDetails(cleanId) ?: return emptyList()
        val songList = details.list ?: details.songs ?: emptyList()
        return songList.mapNotNull {
            it.toDomain(
                fallbackAlbumName = details.name ?: details.title ?: details.album?.name,
                fallbackAlbumId = cleanId,
                fallbackArtwork = details.image ?: details.album?.image
            )
        }
    }

    override suspend fun getArtistAlbums(artistId: String): List<Album> {
        // JioSaavn doesn't have a direct artist albums endpoint, search by artist
        return try {
            val response = api.searchSongs("album artist:" + artistId.removePrefix("jiosaavn_"))
            response.results
                .filter { it.albumId != null }
                .groupBy { it.albumId }
                .values
                .mapNotNull { group -> group.first().toAlbumDomain() }
                .distinctBy { it.id }
                .take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getNewReleases(): List<Album> {
        return try {
            val response = api.getNewReleases()
            val list = response.albums.mapNotNull { it.toDomain() }
            if (list.isNotEmpty()) {
                list
            } else {
                // Fallback: extract distinct albums from trending songs
                getTrending().mapNotNull { track ->
                    if (!track.albumId.isNullOrBlank() && !track.albumName.isNullOrBlank()) {
                        Album(
                            id = track.albumId,
                            name = track.albumName,
                            artist = track.artist,
                            artworkUrl = track.artworkUrl,
                            trackCount = 1,
                            year = track.year,
                            type = Album.AlbumType.ALBUM
                        )
                    } else null
                }.distinctBy { it.id }.take(15)
            }
        } catch (e: Exception) {
            try {
                getTrending().mapNotNull { track ->
                    if (!track.albumId.isNullOrBlank() && !track.albumName.isNullOrBlank()) {
                        Album(
                            id = track.albumId,
                            name = track.albumName,
                            artist = track.artist,
                            artworkUrl = track.artworkUrl,
                            trackCount = 1,
                            year = track.year,
                            type = Album.AlbumType.ALBUM
                        )
                    } else null
                }.distinctBy { it.id }.take(15)
            } catch (ignored: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun searchAlbums(query: String): List<Album> {
        return try {
            val response = api.searchSongs(query + " album")
            response.results
                .filter { it.albumId != null }
                .groupBy { it.albumId }
                .values
                .mapNotNull { group -> group.first().toAlbumDomain() }
                .distinctBy { it.id }
                .take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // NEW: Artist operations
    override suspend fun getArtist(artistId: String): Artist? {
        val cleanId = artistId.removePrefix("jiosaavn_")
        return try {
            val response = api.getArtistDetails(cleanId)
            val artistObj = response.artist
            if (artistObj != null) {
                artistObj.toDomain()
            } else if (!response.name.isNullOrBlank()) {
                Artist(
                    id = "jiosaavn_${response.artistId ?: response.id ?: cleanId}",
                    name = JioSaavnUtils.cleanText(response.name),
                    artworkUrl = JioSaavnUtils.formatImageUrl(response.image),
                    followerCount = response.followerCount?.toLongOrNull(),
                    bio = JioSaavnUtils.cleanText(response.bio).ifBlank { null },
                    genres = response.genre?.let { listOf(JioSaavnUtils.cleanText(it)).filter { it.isNotBlank() } } ?: emptyList(),
                    verified = response.verified?.toBoolean() ?: false
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getArtistTopTracks(artistId: String): List<Track> {
        val cleanId = artistId.removePrefix("jiosaavn_")
        return try {
            val response = api.getArtistTopSongs(cleanId)
            val list = response.songs.mapNotNull { it.toDomain() }
            if (list.isNotEmpty()) {
                list
            } else {
                val details = api.getArtistDetails(cleanId)
                details.topSongs?.mapNotNull { it.toDomain() } ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getSimilarArtists(artistId: String): List<Artist> {
        return try {
            // Search for similar artists by genre
            val artist = getArtist(artistId)
            val genre = artist?.genres?.firstOrNull() ?: artist?.primaryGenre
            if (!genre.isNullOrBlank()) {
                val response = api.searchSongs("artist genre:$genre")
                response.results
                    .mapNotNull { it.toArtistDomain() }
                    .distinctBy { it.id }
                    .filter { it.id != artistId }
                    .take(10)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchArtists(query: String): List<Artist> {
        return try {
            val response = api.searchSongs("artist:" + query)
            response.results
                .mapNotNull { it.toArtistDomain() }
                .distinctBy { it.id }
                .take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // NEW: Browse/Discover
    override suspend fun getFeaturedPlaylists(): List<Playlist> {
        return try {
            val response = api.getFeaturedPlaylists()
            response.playlists.mapNotNull { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCategories(): List<Category> {
        // JioSaavn doesn't have a direct categories endpoint, return common genres
        return listOf(
            Category("bollywood", "Bollywood", null, "Latest Hindi movie songs"),
            Category("punjabi", "Punjabi", null, "Punjabi hits"),
            Category("tamil", "Tamil", null, "Tamil cinema songs"),
            Category("telugu", "Telugu", null, "Telugu movie songs"),
            Category("marathi", "Marathi", null, "Marathi music"),
            Category("bengali", "Bengali", null, "Bengali songs"),
            Category("gujarati", "Gujarati", null, "Gujarati music"),
            Category("kannada", "Kannada", null, "Kannada songs"),
            Category("malayalam", "Malayalam", null, "Malayalam music"),
            Category("devotional", "Devotional", null, "Bhajans & Aartis"),
            Category("ghazal", "Ghazal", null, "Classic Ghazals"),
            Category("retro", "Retro", null, "Old classics"),
        )
    }

    override suspend fun getCategoryPlaylists(categoryId: String): List<Playlist> {
        return try {
            val response = api.searchSongs(categoryId + " playlist")
            response.results
                .filter { it.album != null }
                .groupBy { it.album }
                .values
                .mapNotNull { group ->
                    val first = group.first()
                    Playlist(
                        id = first.albumId?.let { it.hashCode().toLong() } ?: 0,
                        name = first.album ?: "Unknown",
                        trackIds = group.map { it.id }
                    )
                }
                .take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getRecommendations(seedTracks: List<String>, seedArtists: List<String>): List<Track> {
        return try {
            val query = (seedArtists.firstOrNull() ?: seedTracks.firstOrNull() ?: "hindi")
            val response = api.searchSongs(query)
            response.results
                .mapNotNull { it.toDomain() }
                .filter { it.id !in seedTracks }
                .take(20)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // NEW: Lyrics
    override suspend fun getLyrics(trackId: String): String? {
        return try {
            val cleanId = trackId.removePrefix("jiosaavn_")
            val response = api.getLyrics(cleanId)
            JioSaavnUtils.cleanText(response.lyrics).ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    private fun JioSaavnAlbumDetailsResponse.toDomain(cleanId: String): Album {
        val resolvedName = JioSaavnUtils.cleanText(name ?: title ?: album?.name ?: "").ifBlank { "Unknown Album" }
        val resolvedArtist = JioSaavnUtils.cleanText(primaryArtists ?: singers ?: album?.primaryArtists ?: "").ifBlank { "Various Artists" }
        val resolvedArtwork = JioSaavnUtils.formatImageUrl(image ?: album?.image)
        val songList = list ?: songs ?: emptyList()
        val count = songCount?.toIntOrNull() ?: listCount?.toIntOrNull() ?: album?.songCount?.toIntOrNull() ?: songList.size
        val parsedYear = year?.toIntOrNull() ?: releaseDate?.take(4)?.toIntOrNull()
        val parsedType = try {
            type?.uppercase()?.let { Album.AlbumType.valueOf(it) } ?: Album.AlbumType.ALBUM
        } catch (e: Exception) {
            Album.AlbumType.ALBUM
        }

        return Album(
            id = "jiosaavn_$cleanId",
            name = resolvedName,
            artist = resolvedArtist,
            artistId = null,
            artworkUrl = resolvedArtwork,
            trackCount = count,
            durationMs = (duration?.toDoubleOrNull()?.let { (it * 1000).toInt() } ?: 0),
            releaseDate = releaseDate ?: year,
            genre = JioSaavnUtils.cleanText(genre).ifBlank { null },
            year = parsedYear,
            type = parsedType,
            explicit = explicitContent?.toBoolean() ?: false,
            popularity = 0
        )
    }

    private fun JioSaavnSong.toDomain(
        fallbackAlbumName: String? = null,
        fallbackAlbumId: String? = null,
        fallbackArtwork: String? = null
    ): Track? {
        val rawEncrypted = encryptedMediaUrl ?: moreInfo?.encryptedMediaUrl
        val stream = JioSaavnUtils.decryptMediaUrl(rawEncrypted)

        val rawTitle = song.ifBlank { title ?: "" }
        val titleClean = JioSaavnUtils.cleanText(rawTitle).ifBlank { "Untitled Song" }
        val rawArtist = primaryArtists.ifBlank { singers }
        val artistClean = JioSaavnUtils.cleanText(rawArtist).ifBlank { "Various Artists" }
        val artwork = JioSaavnUtils.formatImageUrl(image ?: fallbackArtwork)
        val durationSec = duration.toIntOrNull() ?: moreInfo?.duration?.toIntOrNull() ?: 180

        val resolvedAlbumId = (albumId ?: moreInfo?.albumId ?: fallbackAlbumId)?.let { "jiosaavn_$it" }
        val resolvedAlbumName = JioSaavnUtils.cleanText(album ?: fallbackAlbumName).ifBlank { null }

        return Track(
            id = id,
            title = titleClean,
            artist = artistClean,
            artistId = artistId,
            albumId = resolvedAlbumId,
            albumName = resolvedAlbumName,
            albumArtworkUrl = artwork,
            artworkUrl = artwork,
            durationMs = durationSec * 1000,
            streamUrl = stream,
            trackNumber = trackNumber?.toIntOrNull() ?: 0,
            year = year?.toIntOrNull(),
            genre = JioSaavnUtils.cleanText(genre).ifBlank { null },
            explicit = explicitContent?.toBoolean() ?: false,
            popularity = popularity?.toIntOrNull() ?: 0
        )
    }

    private fun JioSaavnSong.toAlbumDomain(): Album? {
        return try {
            val name = JioSaavnUtils.cleanText(album).ifBlank { return null }
            val artist = JioSaavnUtils.cleanText(primaryArtists.ifBlank { singers }).ifBlank { "Various Artists" }
            val artwork = JioSaavnUtils.formatImageUrl(image)
            val albumId = this.albumId ?: return null

            Album(
                id = "jiosaavn_$albumId",
                name = name,
                artist = artist,
                artistId = artistId?.let { "jiosaavn_$it" },
                artworkUrl = artwork,
                trackCount = 1, // Will be updated when fetching album details
                durationMs = duration.toIntOrNull() ?: 0,
                releaseDate = releaseDate ?: year,
                genre = JioSaavnUtils.cleanText(genre).ifBlank { null },
                year = year?.toIntOrNull(),
                type = Album.AlbumType.ALBUM,
                explicit = explicitContent?.toBoolean() ?: false,
                popularity = popularity?.toIntOrNull() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun JioSaavnSong.toArtistDomain(): Artist? {
        return try {
            val name = JioSaavnUtils.cleanText(primaryArtists.ifBlank { singers }).ifBlank { return null }
            val artistId = this.artistId ?: return null

            Artist(
                id = "jiosaavn_$artistId",
                name = name,
                artworkUrl = JioSaavnUtils.formatImageUrl(image),
                genres = genre?.let { listOf(JioSaavnUtils.cleanText(it)).filter { it.isNotBlank() } } ?: emptyList(),
                popularity = popularity?.toIntOrNull() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun JioSaavnAlbum.toDomain(): Album {
        val parsedType = try {
            type?.uppercase()?.let { Album.AlbumType.valueOf(it) } ?: Album.AlbumType.ALBUM
        } catch (e: Exception) {
            Album.AlbumType.ALBUM
        }
        val cleanName = JioSaavnUtils.cleanText(name.ifBlank { title ?: "" }).ifBlank { "Unknown Album" }
        val cleanArtist = JioSaavnUtils.cleanText(primaryArtists).ifBlank { "Various Artists" }

        return Album(
            id = "jiosaavn_$id",
            name = cleanName,
            artist = cleanArtist,
            artistId = null,
            artworkUrl = JioSaavnUtils.formatImageUrl(image),
            trackCount = songCount.toIntOrNull() ?: 0,
            durationMs = (duration.toDoubleOrNull()?.let { (it * 1000).toInt() } ?: 0),
            releaseDate = releaseDate ?: year,
            genre = JioSaavnUtils.cleanText(genre).ifBlank { null },
            year = year?.toIntOrNull(),
            type = parsedType,
            explicit = explicitContent?.toBoolean() ?: false,
            popularity = 0
        )
    }

    private fun JioSaavnArtist.toDomain(): Artist {
        return Artist(
            id = "jiosaavn_$id",
            name = JioSaavnUtils.cleanText(name),
            artworkUrl = JioSaavnUtils.formatImageUrl(image),
            followerCount = followerCount?.toLongOrNull(),
            bio = JioSaavnUtils.cleanText(bio).ifBlank { null },
            genres = genre?.let { listOf(JioSaavnUtils.cleanText(it)).filter { it.isNotBlank() } } ?: emptyList(),
            verified = verified?.toBoolean() ?: false
        )
    }

    private fun JioSaavnPlaylist.toDomain(): Playlist? {
        return try {
            val name = JioSaavnUtils.cleanText(name).ifBlank { return null }
            Playlist(
                id = id.hashCode().toLong(),
                name = name,
                trackIds = emptyList() // Would need separate call
            )
        } catch (e: Exception) {
            null
        }
    }
}
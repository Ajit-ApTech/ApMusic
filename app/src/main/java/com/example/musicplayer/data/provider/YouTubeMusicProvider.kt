package com.example.musicplayer.data.provider

import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.util.EncryptedPrefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeMusicProvider @Inject constructor(
    private val api: YouTubeMusicApi,
    private val encryptedPrefs: EncryptedPrefs
) : MusicProvider {

    override val providerId: String = "ytmusic"
    override val displayName: String = "YouTube Music"
    override val requiresApiKey: Boolean = false

    companion object {
        /**
         * List of Invidious public instances.
         * Ordered by reliability — first ones are prioritised.
         * Only instances that return valid JSON API responses are included.
         * Update this list periodically from: https://api.invidious.io/instances.json
         */
        val FALLBACK_INSTANCES = listOf(
            "https://invidious.f5.si",           // Verified working ✓
            "https://invidious.protokolla.fi",    // Backup
            "https://invidious.perennialte.ch",   // Backup
            "https://invidious.drgns.space",      // Backup
            "https://invidious.fdn.fr",           // European backup
            "https://iv.ggtyler.dev",             // Backup
            "https://yewtu.be"                    // Last resort
        )
    }

    // Cache the last known-good instance during the app session
    private var cachedWorkingInstance: String? = null

    private fun getCandidateInstances(): List<String> {
        val saved = encryptedPrefs.getApiKey(providerId)?.trim()?.trimEnd('/')
        return buildList {
            // User-configured custom instance first
            if (!saved.isNullOrBlank()) add(saved)
            // Then cached working instance from previous call
            cachedWorkingInstance?.let { if (it !in this) add(it) }
            // Then the full fallback list
            addAll(FALLBACK_INSTANCES.filter { it !in this })
        }
    }

    /**
     * Tries each instance in order until one returns valid non-empty results.
     * Caches the working instance for the session to avoid repeated retries.
     */
    private suspend fun queryInvidious(query: String): Pair<List<YouTubeVideoItem>, String> {
        val instances = getCandidateInstances()
        for (instance in instances) {
            try {
                val items = api.search(
                    url = "$instance/api/v1/search",
                    query = query
                )
                // Validate: must return a non-empty list of real video objects
                val valid = items.filter { it.videoId.isNotBlank() }
                if (valid.isNotEmpty()) {
                    cachedWorkingInstance = instance   // remember what worked
                    return Pair(valid, instance)
                }
            } catch (e: Exception) {
                // Instance failed (network error, wrong content type, timeout) → try next
            }
        }
        // All instances failed — return empty with first fallback as base for stream URLs
        val fallback = cachedWorkingInstance ?: FALLBACK_INSTANCES.first()
        return Pair(emptyList(), fallback)
    }

    override suspend fun search(query: String): List<Track> {
        val q = query.ifBlank { "Hindi Top Hits" }
        val (items, workingBase) = queryInvidious(q)
        return items.map { it.toDomain(workingBase) }
    }

    override suspend fun getTrending(): List<Track> {
        val (items, workingBase) = queryInvidious("Top Bollywood Songs 2026")
        return items.map { it.toDomain(workingBase) }
    }

    /**
     * Returns a stream URL using the working/cached instance.
     * itag=140 = m4a 128kbps audio-only (best quality for music).
     * Falls back to itag=18 (360p with audio) if needed.
     */
    override suspend fun getTrackStreamUrl(trackId: String): String {
        val base = cachedWorkingInstance ?: FALLBACK_INSTANCES.first()
        return "$base/latest_version?id=$trackId&itag=140&local=true"
    }

    /**
     * Tests connection by trying all instances.
     * Returns true if ANY instance returns valid results.
     */
    override suspend fun testConnection(apiKey: String?): Boolean {
        val customUrl = apiKey?.trim()?.trimEnd('/')
        if (!customUrl.isNullOrBlank()) {
            return try {
                val items = api.search(url = "$customUrl/api/v1/search", query = "hindi music")
                items.any { it.videoId.isNotBlank() }
            } catch (e: Exception) {
                false
            }
        }
        // Test with a query — use queryInvidious which tries all instances
        val (items, _) = queryInvidious("hindi music")
        return items.isNotEmpty()
    }

    private fun YouTubeVideoItem.toDomain(baseInstanceUrl: String): Track {
        // Prefer high/medium quality thumbnail; fallback to YouTube's direct CDN
        val thumb = videoThumbnails
            .filter { !it.url.isNullOrBlank() }
            .firstOrNull { it.quality == "high" || it.quality == "medium" }
            ?.url
            ?: videoThumbnails.firstOrNull { !it.url.isNullOrBlank() }?.url
            ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

        val resolvedThumb = when {
            thumb.startsWith("//") -> "https:$thumb"
            thumb.startsWith("/") -> "$baseInstanceUrl$thumb"
            else -> thumb
        }

        return Track(
            id = videoId,
            title = title.ifBlank { "Untitled Video" },
            artist = author.ifBlank { "YouTube Artist" },
            artworkUrl = resolvedThumb,
            durationMs = lengthSeconds * 1000,
            streamUrl = "$baseInstanceUrl/latest_version?id=$videoId&itag=140&local=true"
        )
    }
}

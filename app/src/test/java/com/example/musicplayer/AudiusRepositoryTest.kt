package com.example.musicplayer

import com.example.musicplayer.data.local.FavoriteDao
import com.example.musicplayer.data.local.PlaylistDao
import com.example.musicplayer.data.local.QueueDao
import com.example.musicplayer.data.local.RecentPlayDao
import com.example.musicplayer.data.provider.AudiusProvider
import com.example.musicplayer.data.provider.FmaProvider
import com.example.musicplayer.data.provider.JamendoProvider
import com.example.musicplayer.data.provider.JioSaavnProvider
import com.example.musicplayer.data.provider.YouTubeMusicProvider
import com.example.musicplayer.data.repository.MusicRepositoryImpl
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.util.EncryptedPrefs
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class AudiusRepositoryTest {

    @Mock
    private lateinit var jioSaavnProvider: JioSaavnProvider

    @Mock
    private lateinit var youTubeMusicProvider: YouTubeMusicProvider

    @Mock
    private lateinit var audiusProvider: AudiusProvider

    @Mock
    private lateinit var fmaProvider: FmaProvider

    @Mock
    private lateinit var jamendoProvider: JamendoProvider

    @Mock
    private lateinit var favoriteDao: FavoriteDao

    @Mock
    private lateinit var recentPlayDao: RecentPlayDao

    @Mock
    private lateinit var playlistDao: PlaylistDao

    @Mock
    private lateinit var queueDao: QueueDao

    @Mock
    private lateinit var encryptedPrefs: EncryptedPrefs

    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(jioSaavnProvider.providerId).thenReturn("jiosaavn")
        Mockito.`when`(jioSaavnProvider.displayName).thenReturn("JioSaavn (Bollywood & Indian)")
        Mockito.`when`(youTubeMusicProvider.providerId).thenReturn("ytmusic")
        Mockito.`when`(youTubeMusicProvider.displayName).thenReturn("YouTube Music")
        Mockito.`when`(audiusProvider.providerId).thenReturn("audius")
        Mockito.`when`(audiusProvider.displayName).thenReturn("Audius")
        Mockito.`when`(fmaProvider.providerId).thenReturn("fma")
        Mockito.`when`(fmaProvider.displayName).thenReturn("Free Music Archive")
        Mockito.`when`(jamendoProvider.providerId).thenReturn("jamendo")
        Mockito.`when`(jamendoProvider.displayName).thenReturn("Jamendo Music")
        Mockito.`when`(encryptedPrefs.getActiveProviderId()).thenReturn("jiosaavn")

        repository = MusicRepositoryImpl(
            jioSaavnProvider = jioSaavnProvider,
            youTubeMusicProvider = youTubeMusicProvider,
            audiusProvider = audiusProvider,
            fmaProvider = fmaProvider,
            jamendoProvider = jamendoProvider,
            favoriteDao = favoriteDao,
            recentPlayDao = recentPlayDao,
            playlistDao = playlistDao,
            queueDao = queueDao,
            encryptedPrefs = encryptedPrefs
        )
    }

    @Test
    fun `getTrending delegates to active provider`() = runBlocking {
        val fakeTracks = listOf(
            Track(id = "1", title = "Kesariya", artist = "Arijit Singh", artworkUrl = null, durationMs = 180000),
            Track(id = "2", title = "Apna Bana Le", artist = "Arijit Singh", artworkUrl = null, durationMs = 200000)
        )
        Mockito.`when`(jioSaavnProvider.getTrending()).thenReturn(fakeTracks)

        val result = repository.getTrending()
        assertEquals(2, result.size)
        assertEquals("Kesariya", result[0].title)
    }

    @Test
    fun `switching active provider updates activeProviderFlow`() = runBlocking {
        assertEquals("jiosaavn", repository.getActiveProvider().providerId)
        repository.setActiveProvider("audius")
        assertEquals("audius", repository.getActiveProvider().providerId)
    }
}

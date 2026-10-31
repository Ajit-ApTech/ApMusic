package com.example.musicplayer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.ui.navigation.Screen
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCard
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelLavender
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelRose
import com.example.musicplayer.ui.theme.PastelTextPrimary
import com.example.musicplayer.ui.theme.PastelTextSecondary
import com.example.musicplayer.util.DownloadState

@Composable
fun HomeScreen(
    navController: NavController,
    onOpenNowPlaying: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val trendingTracks by viewModel.trending.collectAsState()
    val recentPlays by viewModel.recentPlays.collectAsState()
    val newReleases by viewModel.newReleases.collectAsState()
    val popularArtists by viewModel.popularArtists.collectAsState()
    val featuredPlaylists by viewModel.featuredPlaylists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingBrowse by viewModel.isLoadingBrowse.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val activeProvider by viewModel.activeProvider.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState(initial = emptyList())
    val downloadStates by viewModel.downloadStates.collectAsState()
    val downloadedIds = downloadedTracks.map { it.id }.toSet()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PastelDarkBg
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Header: Clean Greeting
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = viewModel.getGreeting(),
                        color = PastelTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )

                    // Active Source Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PastelCard)
                            .border(1.dp, PastelBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(PastelMint)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = activeProvider.displayName,
                            color = PastelMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Error banner if any
            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        color = Color(0xFF281C26),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelRose.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = PastelRose,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connection Notice",
                                    color = PastelTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = PastelTextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { navController.navigate(Screen.Settings.route) },
                                colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Open Settings", color = Color(0xFF1A1202), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // New Releases Section
            if (newReleases.isNotEmpty()) {
                item {
                    HomeSectionHeader(
                        title = "New Releases",
                        subtitle = "${newReleases.size} albums",
                        onSeeAllClick = { navController.navigate(Screen.Search.route) }
                    )
                }
                item {
                    AlbumCarousel(
                        albums = newReleases,
                        onItemClick = { album -> navController.navigate("album/${album.id}") }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Featured Playlists Section
            if (featuredPlaylists.isNotEmpty()) {
                item {
                    HomeSectionHeader(
                        title = "Featured Playlists",
                        subtitle = "${featuredPlaylists.size} playlists",
                        onSeeAllClick = { navController.navigate(Screen.Search.route) }
                    )
                }
                item {
                    PlaylistCarousel(
                        playlists = featuredPlaylists,
                        onItemClick = { /* TODO: playlist detail */ }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Popular Artists Section
            if (popularArtists.isNotEmpty()) {
                item {
                    HomeSectionHeader(
                        title = "Popular Artists",
                        subtitle = "${popularArtists.size} artists",
                        onSeeAllClick = { /* TODO: artists list */ }
                    )
                }
                item {
                    ArtistCarousel(
                        artists = popularArtists,
                        onItemClick = { artist ->
                            navController.navigate("artist/${artist.id}")
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Recently Played Section
            if (recentPlays.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Played",
                            color = PastelTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View all",
                            color = PastelMint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    navController.navigate("${Screen.Library.route}?tab=2") {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentPlays.take(10), key = { "recent_${it.id}" }) { track ->
                            RecentPlayCard(
                                track = track,
                                onClick = {
                                    viewModel.playTrack(track, fromTrending = false)
                                    onOpenNowPlaying()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Trending Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trending Now",
                        color = PastelTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${trendingTracks.size} tracks",
                        color = PastelLavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PastelMint, strokeWidth = 2.5.dp)
                    }
                }
            }

            // Trending Tracks List
            items(trendingTracks, key = { it.id }) { track ->
                val isDownloaded = downloadedIds.contains(track.id)
                val state = downloadStates[track.id]
                TrackRowItem(
                    track = track,
                    isDownloaded = isDownloaded,
                    downloadState = state,
                    onDownloadClick = { viewModel.downloadTrack(track) },
                    onClick = {
                        viewModel.playTrack(track, fromTrending = true)
                        onOpenNowPlaying()
                    }
                )
            }
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    subtitle: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = PastelTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = PastelLavender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = "See all",
            color = PastelMint,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSeeAllClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AlbumCarousel(
    albums: List<Album>,
    onItemClick: (Album) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(albums, key = { "album_${it.id}" }) { album ->
            AlbumCard(
                album = album,
                onClick = { onItemClick(album) }
            )
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    val cardWidth = 148.dp
    Surface(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, PastelBorder, RoundedCornerShape(16.dp)),
        color = PastelCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!album.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = album.artworkUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelCardElevated)
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PastelLavender, modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = album.name,
                color = PastelTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = album.artist,
                color = PastelTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlaylistCarousel(
    playlists: List<Playlist>,
    onItemClick: (Playlist) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(playlists, key = { "playlist_${it.id}" }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onItemClick(playlist) }
            )
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    val cardWidth = 148.dp
    Surface(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, PastelBorder, RoundedCornerShape(16.dp)),
        color = PastelCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!playlist.name.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(PastelLavender.copy(alpha = 0.3f), PastelMint.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = PastelMint, modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playlist.name,
                color = PastelTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ArtistCarousel(
    artists: List<Artist>,
    onItemClick: (Artist) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(artists, key = { "artist_${it.id}" }) { artist ->
            ArtistCard(
                artist = artist,
                onClick = { onItemClick(artist) }
            )
        }
    }
}

@Composable
fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    val cardWidth = 148.dp
    Surface(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, PastelBorder, RoundedCornerShape(16.dp)),
        color = PastelCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!artist.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = artist.artworkUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PastelCardElevated)
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PastelLavender, modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = artist.name,
                color = PastelTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentPlayCard(
    track: Track,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(124.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, PastelBorder, RoundedCornerShape(16.dp)),
        color = PastelCard
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PastelCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PastelLavender)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = track.title,
                color = PastelTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                color = PastelTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TrackRowItem(
    track: Track,
    modifier: Modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
    isDownloaded: Boolean = false,
    downloadState: DownloadState? = null,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, PastelBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
        color = PastelCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PastelCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PastelLavender)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = PastelTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = track.artist,
                    color = PastelTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Download Icon Button
            if (onDownloadClick != null) {
                when {
                    isDownloaded || downloadState is DownloadState.Downloaded -> {
                        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Downloaded",
                                tint = PastelMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    downloadState is DownloadState.Downloading -> {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PastelMint,
                                strokeWidth = 2.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    else -> {
                        IconButton(onClick = onDownloadClick, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = "Save Offline",
                                tint = PastelLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Play button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PastelMint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = PastelMint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

package com.example.musicplayer.ui.screens.album

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCard
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelLavender
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelPeach
import com.example.musicplayer.ui.theme.PastelRose
import com.example.musicplayer.ui.theme.PastelTextPrimary
import com.example.musicplayer.ui.theme.PastelTextSecondary
import com.example.musicplayer.util.DownloadState

@Composable
fun AlbumDetailScreen(
    albumId: String,
    navController: NavController,
    onOpenNowPlaying: () -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(albumId) {
        if (albumId.isNotBlank()) {
            viewModel.loadAlbum(albumId)
        }
    }

    val album by viewModel.album.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState(initial = emptyList())
    val downloadStates by viewModel.downloadStates.collectAsState()
    val downloadedIds = downloadedTracks.map { it.id }.toSet()

    val currentAlbum = album

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PastelDarkBg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (currentAlbum == null) {
                // Top navigation bar for error/loading state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PastelCardElevated)
                            .border(1.dp, PastelBorder, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PastelTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = PastelMint,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading album...",
                                color = PastelTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(PastelCardElevated)
                                    .border(1.dp, PastelBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = PastelLavender,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Album not found",
                                color = PastelTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = PastelTextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 3,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.loadAlbum(albumId) },
                                colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Retry", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // Top App Bar + Hero Header with Aurora Glow
                    item {
                        AlbumHeroHeader(
                            album = currentAlbum,
                            tracksCount = tracks.size,
                            onBackClick = { navController.popBackStack() },
                            onPlayClick = {
                                if (tracks.isNotEmpty()) {
                                    viewModel.playAlbum(tracks)
                                    onOpenNowPlaying()
                                }
                            },
                            onShuffleClick = {
                                if (tracks.isNotEmpty()) {
                                    viewModel.shuffleAlbum(tracks)
                                    onOpenNowPlaying()
                                }
                            },
                            onDownloadClick = {
                                viewModel.downloadAlbum(tracks)
                            }
                        )
                    }

                    // Section Divider & Tracks Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRACKS",
                                color = PastelLavender,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "${tracks.size} songs",
                                color = PastelTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Track List
                    if (tracks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = PastelMint, strokeWidth = 2.5.dp)
                                } else {
                                    Text(
                                        text = "No songs found in this album",
                                        color = PastelTextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(tracks, key = { _, track -> "album_track_${track.id}" }) { index, track ->
                            val isDownloaded = downloadedIds.contains(track.id)
                            val state = downloadStates[track.id]
                            AlbumTrackRowItem(
                                track = track,
                                index = index + 1,
                                isDownloaded = isDownloaded,
                                downloadState = state,
                                onDownloadClick = { viewModel.downloadTrack(track) },
                                onClick = {
                                    viewModel.playTrack(track, tracks)
                                    onOpenNowPlaying()
                                }
                            )
                            if (index < tracks.lastIndex) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumHeroHeader(
    album: Album,
    tracksCount: Int,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PastelMint.copy(alpha = 0.22f),
                        PastelCard.copy(alpha = 0.6f),
                        PastelDarkBg
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PastelCardElevated.copy(alpha = 0.85f))
                        .border(1.dp, PastelBorder, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PastelTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    color = PastelMint.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelMint.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "ALBUM",
                        color = PastelLavender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            // Hero Artwork & Info Centered
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = PastelMint.copy(alpha = 0.35f),
                            spotColor = PastelLavender.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(PastelCard)
                        .border(1.dp, PastelBorder, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!album.artworkUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = album.artworkUrl,
                            contentDescription = album.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(PastelMint.copy(alpha = 0.4f), PastelLavender.copy(alpha = 0.2f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = PastelMint,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Album Name
                Text(
                    text = album.name,
                    color = PastelTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Artist Name
                Text(
                    text = album.artist,
                    color = PastelTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata tags row: Year • Count • Duration
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val yearText = album.year?.toString() ?: album.releaseDate?.take(4)
                    if (!yearText.isNullOrBlank()) {
                        Text(text = yearText, color = PastelLavender, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = "•", color = PastelTextSecondary, fontSize = 12.sp)
                    }
                    val count = if (tracksCount > 0) tracksCount else album.trackCount
                    Text(text = "$count songs", color = PastelTextSecondary, fontSize = 12.sp)
                    if (album.durationMs > 0) {
                        Text(text = "•", color = PastelTextSecondary, fontSize = 12.sp)
                        Text(text = formatDuration(album.durationMs.toLong()), color = PastelTextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Action Buttons: Play All + Shuffle + Download
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play All Primary Button
                    Button(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .height(48.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = PastelMint.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play All",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Play All",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Shuffle Secondary Pill
                    Button(
                        onClick = onShuffleClick,
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PastelCardElevated,
                            contentColor = PastelTextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelBorder)
                    ) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = PastelLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Shuffle",
                            color = PastelTextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Download Action Pill
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PastelCardElevated)
                            .border(1.dp, PastelBorder, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = "Download Album",
                            tint = PastelMint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumTrackRowItem(
    track: Track,
    index: Int,
    isDownloaded: Boolean,
    downloadState: DownloadState?,
    onDownloadClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, PastelBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        color = PastelCardElevated.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Index Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PastelDarkBg.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d".format(index),
                    color = PastelLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Track Title & Artist
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = PastelTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    color = PastelTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Track Duration
            Text(
                text = track.formattedDuration,
                color = PastelTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 4.dp)
            )

            // Download Status / Action
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
                            .padding(8.dp),
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
                            tint = PastelTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remMinutes = minutes % 60
        "${hours}h ${remMinutes}m"
    } else {
        "${minutes}m ${seconds}s"
    }
}

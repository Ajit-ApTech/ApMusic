package com.example.musicplayer.ui.screens.artist

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
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
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.Track
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
fun ArtistDetailScreen(
    artistId: String,
    navController: NavController,
    onOpenNowPlaying: () -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(artistId) {
        if (artistId.isNotBlank()) {
            viewModel.loadArtist(artistId)
        }
    }
    val artist by viewModel.artist.collectAsState()
    val topTracks by viewModel.topTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState(initial = emptyList())
    val downloadStates by viewModel.downloadStates.collectAsState()
    val downloadedIds = downloadedTracks.map { it.id }.toSet()

    val currentArtist = artist
    val currentErrorMessage = errorMessage

    if (currentArtist == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = PastelDarkBg) {
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PastelTextPrimary
                    )
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = PastelMint, strokeWidth = 2.5.dp)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PastelLavender, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Artist not found", color = PastelTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            if (currentErrorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(currentErrorMessage, color = PastelTextSecondary, fontSize = 13.sp, maxLines = 3, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadArtist(artistId) },
                                colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PastelDarkBg) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Hero Header with Artist Info
            item {
                ArtistHeroHeader(artist = currentArtist, onOpenNowPlaying = onOpenNowPlaying, navController = navController)
            }

            // Artist Info Section
            item {
                ArtistInfoSection(artist = currentArtist)
            }

            // Top Tracks Section
            if (topTracks.isEmpty()) {
                item {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PastelMint, strokeWidth = 2.5.dp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tracks found for this artist", color = PastelTextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Top Tracks",
                            color = PastelTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(topTracks, key = { "artist_track_${it.id}" }) { track ->
                    val isDownloaded = downloadedIds.contains(track.id)
                    val state = downloadStates[track.id]
                    ArtistTrackRowItem(
                        track = track,
                        trackNumber = topTracks.indexOf(track) + 1,
                        isDownloaded = isDownloaded,
                        downloadState = state,
                        onDownloadClick = { viewModel.downloadTrack(track) },
                        onClick = {
                            viewModel.playTrack(track, topTracks)
                            onOpenNowPlaying()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistHeroHeader(
    artist: Artist,
    navController: NavController,
    onOpenNowPlaying: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Artist Artwork
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .border(1.dp, PastelBorder.copy(alpha = 0.3f), CircleShape)
            ) {
                if (!artist.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = artist.artworkUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PastelLavender.copy(alpha = 0.3f), PastelMint.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PastelMint, modifier = Modifier.size(48.dp))
                    }
                }
            }

            Column(
                modifier = Modifier.padding(start = 16.dp).weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                // Verified badge
                if (artist.verified) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Verified", tint = Color(0xFF1DA1F2), modifier = Modifier.size(16.dp))
                        Text("Verified", color = PastelLavender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = artist.name,
                    color = PastelTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Genres
                if (artist.genres.isNotEmpty()) {
                    Text(
                        text = artist.genres.joinToString(", "),
                        color = PastelTextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Followers
                if (artist.followerCount != null && artist.followerCount > 0) {
                    Text(
                        text = "${artist.formattedFollowers} followers",
                        color = PastelTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistInfoSection(artist: Artist) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Bio section
        if (artist.bio != null && artist.bio.isNotBlank()) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "About",
                    color = PastelTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = artist.bio ?: "",
                    color = PastelTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (artist.genres.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(artist.genres, key = { it }) { genre ->
                    Surface(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = PastelCardElevated,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = genre.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            color = PastelMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistTrackRowItem(
    track: Track,
    trackNumber: Int,
    isDownloaded: Boolean = false,
    downloadState: DownloadState? = null,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, PastelBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
        color = PastelCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Number
            Text(
                text = trackNumber.toString(),
                color = PastelTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(28.dp)
            )

            // Artwork
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PastelCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PastelLavender)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Track Info
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
                    text = track.displayAlbum ?: track.artist,
                    color = PastelTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Duration
            Text(
                text = track.formattedDuration,
                color = PastelTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 8.dp)
            )

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

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
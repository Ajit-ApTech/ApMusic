package com.example.musicplayer.ui.screens.library

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.ui.screens.home.TrackRowItem
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCard
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelLavender
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelRose
import com.example.musicplayer.ui.theme.PastelTextPrimary
import com.example.musicplayer.ui.theme.PastelTextSecondary

import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    onOpenNowPlaying: () -> Unit,
    initialTab: Int = 0,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val downloadStates by viewModel.downloadStates.collectAsState()
    val downloadedIds = downloadedTracks.map { it.id }.toSet()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(initialTab) {
        if (initialTab in 0..3) {
            viewModel.selectTab(initialTab)
        }
    }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

    val tabs = listOf("Liked Songs", "Playlists", "Recently Played", "Downloaded")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PastelDarkBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    color = PastelTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                if (selectedTab == 1) {
                    IconButton(onClick = { showCreatePlaylistDialog = true }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "New Playlist",
                            tint = PastelMint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Filter Chips with pastel pill styling
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs.indices.toList()) { index ->
                    val isSelected = selectedTab == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        label = {
                            Text(
                                text = tabs[index],
                                color = if (isSelected) Color(0xFF1A1202) else PastelTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelMint,
                            containerColor = PastelCard
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) PastelMint else PastelBorder,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> { // Liked Songs
                    if (favorites.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(PastelRose.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = PastelRose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Songs You Love Will Appear Here",
                                    color = PastelTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap the heart icon on any song to add it",
                                    color = PastelTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 110.dp)
                        ) {
                            items(favorites, key = { "fav_${it.id}" }) { track ->
                                TrackRowItem(
                                    track = track,
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    isDownloaded = downloadedIds.contains(track.id),
                                    downloadState = downloadStates[track.id],
                                    onDownloadClick = { viewModel.downloadTrack(track) },
                                    onClick = {
                                        viewModel.playTrack(track, favorites)
                                        onOpenNowPlaying()
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> { // Playlists
                    if (playlists.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(PastelLavender.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = PastelLavender,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Create Your First Playlist",
                                    color = PastelTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showCreatePlaylistDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Create Playlist", color = Color(0xFF1A1202), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 110.dp)
                        ) {
                            items(playlists, key = { "pl_${it.id}" }) { pl ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, PastelBorder, RoundedCornerShape(14.dp))
                                        .clickable { selectedPlaylist = pl },
                                    color = PastelCard
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    Brush.linearGradient(listOf(PastelLavender.copy(alpha = 0.3f), PastelMint.copy(alpha = 0.2f)))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = PastelMint)
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pl.name,
                                                color = PastelTextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Playlist • Tap to view songs",
                                                color = PastelTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }

                                        IconButton(onClick = { viewModel.deletePlaylist(pl.id) }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Delete",
                                                tint = PastelTextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // History (Recently Played)
                    if (recentlyPlayed.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recently played tracks yet",
                                color = PastelTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 110.dp)
                        ) {
                            items(recentlyPlayed, key = { "history_${it.id}" }) { track ->
                                TrackRowItem(
                                    track = track,
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    isDownloaded = downloadedIds.contains(track.id),
                                    downloadState = downloadStates[track.id],
                                    onDownloadClick = { viewModel.downloadTrack(track) },
                                    onClick = {
                                        viewModel.playTrack(track, recentlyPlayed)
                                        onOpenNowPlaying()
                                    }
                                )
                            }
                        }
                    }
                }
                3 -> { // Downloaded (Offline)
                    if (downloadedTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(PastelMint.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Download,
                                        contentDescription = null,
                                        tint = PastelMint,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Downloaded Songs",
                                    color = PastelTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Download songs to play offline without internet",
                                    color = PastelTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 110.dp)
                        ) {
                            items(downloadedTracks, key = { "dl_${it.id}" }) { track ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        TrackRowItem(
                                            track = track,
                                            modifier = Modifier.padding(vertical = 5.dp),
                                            onClick = {
                                                viewModel.playTrack(track, downloadedTracks)
                                                onOpenNowPlaying()
                                            }
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteDownload(track.id) }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete download",
                                            tint = PastelRose,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedPlaylist?.let { pl ->
            var playlistTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
            LaunchedEffect(pl.id) {
                viewModel.getPlaylistTracks(pl.id).collect { tracks ->
                    playlistTracks = tracks
                }
            }

            ModalBottomSheet(
                onDismissRequest = { selectedPlaylist = null },
                containerColor = PastelDarkBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pl.name,
                                color = PastelTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${playlistTracks.size} songs",
                                color = PastelTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        if (playlistTracks.isNotEmpty()) {
                            Button(
                                onClick = {
                                    viewModel.playTrack(playlistTracks.first(), playlistTracks)
                                    onOpenNowPlaying()
                                    selectedPlaylist = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color(0xFF1A1202))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play All", color = Color(0xFF1A1202), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (playlistTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No songs in this playlist yet.\nAdd songs from Now Playing screen!",
                                color = PastelTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(350.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(playlistTracks, key = { "pl_track_${it.id}" }) { track ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        TrackRowItem(
                                            track = track,
                                            modifier = Modifier.padding(vertical = 5.dp),
                                            onClick = {
                                                viewModel.playTrack(track, playlistTracks)
                                                onOpenNowPlaying()
                                                selectedPlaylist = null
                                            }
                                        )
                                    }
                                    IconButton(onClick = { viewModel.removeTrackFromPlaylist(pl.id, track.id) }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Remove from playlist",
                                            tint = PastelRose,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreatePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("New Playlist", color = PastelTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("Playlist name", color = PastelTextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PastelCardElevated,
                            unfocusedContainerColor = PastelCardElevated,
                            focusedTextColor = PastelTextPrimary,
                            unfocusedTextColor = PastelTextPrimary,
                            focusedBorderColor = PastelMint,
                            unfocusedBorderColor = PastelBorder
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create", color = Color(0xFF1A1202), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Cancel", color = PastelTextSecondary)
                    }
                },
                containerColor = PastelCard,
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}



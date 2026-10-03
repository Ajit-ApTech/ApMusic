package com.example.musicplayer.ui.screens.localfiles

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCard
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelLavender
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelTextPrimary
import com.example.musicplayer.ui.theme.PastelTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalFilesScreen(
    onOpenNowPlaying: () -> Unit,
    viewModel: LocalFilesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    // Permission launcher
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.scanDeviceAudio()
        else viewModel.onPermissionDenied()
    }

    LaunchedEffect(Unit) {
        // Only launch permission dialog if not already granted
        // This prevents crashes in gaming mode / restricted environments
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val alreadyGranted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            viewModel.scanDeviceAudio()
        } else {
            try {
                permissionLauncher.launch(permission)
            } catch (e: Exception) {
                // Gaming mode or restricted environment blocked the dialog
                viewModel.onPermissionDenied()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelDarkBg)
    ) {
        // ─── Header ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PastelLavender.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = null,
                        tint = PastelMint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "My Files",
                        color = PastelTextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Audio files on your device",
                    color = PastelTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, start = 38.dp)
                )
            }
        }

        when (val s = state) {

            // ─── Loading ──────────────────────────────────────────
            is LocalFilesState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PastelMint, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Scanning device audio...", color = PastelTextSecondary, fontSize = 14.sp)
                    }
                }
            }

            // ─── Permission Required ───────────────────────────────
            is LocalFilesState.PermissionRequired -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = PastelLavender,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Storage Access Required",
                            color = PastelTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Grant storage permission to browse and play music files already on your device.",
                            color = PastelTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Button(
                            onClick = {
                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                permissionLauncher.launch(permission)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelMint),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp)
                        ) {
                            Text("Grant Storage Access", color = PastelDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ─── Empty ────────────────────────────────────────────
            is LocalFilesState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.AudioFile,
                            contentDescription = null,
                            tint = PastelTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No audio files found",
                            color = PastelTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Download songs via ApMusic or\nadd MP3 files to your device storage.",
                            color = PastelTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.scanDeviceAudio() },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = PastelMint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refresh", color = PastelTextPrimary)
                        }
                    }
                }
            }

            // ─── Success ──────────────────────────────────────────
            is LocalFilesState.Success -> {
                val tracks = s.tracks

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Search in My Files...", color = PastelTextSecondary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = PastelTextSecondary, modifier = Modifier.size(20.dp))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PastelMint,
                        unfocusedBorderColor = PastelBorder,
                        focusedTextColor = PastelTextPrimary,
                        unfocusedTextColor = PastelTextPrimary,
                        cursorColor = PastelMint
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Sort chips
                val sortLabels = listOf(
                    LocalSortOrder.DATE_ADDED_NEWEST to "Newest",
                    LocalSortOrder.DATE_ADDED_OLDEST to "Oldest",
                    LocalSortOrder.TITLE_AZ to "A-Z",
                    LocalSortOrder.ARTIST_AZ to "Artist"
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortLabels.size) { i ->
                        val (order, label) = sortLabels[i]
                        FilterChip(
                            selected = sortOrder == order,
                            onClick = { viewModel.onSortOrderChanged(order) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelMint.copy(alpha = 0.25f),
                                selectedLabelColor = PastelMint,
                                containerColor = PastelCard,
                                labelColor = PastelTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Play All / Shuffle row + track count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tracks.size} songs",
                        color = PastelTextSecondary,
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Play All
                        Button(
                            onClick = { viewModel.playAll(tracks); onOpenNowPlaying() },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelMint.copy(alpha = 0.18f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = PastelMint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play All", color = PastelMint, fontSize = 13.sp)
                        }
                        // Shuffle All
                        Button(
                            onClick = { viewModel.shuffleAll(tracks); onOpenNowPlaying() },
                            colors = ButtonDefaults.buttonColors(containerColor = PastelLavender.copy(alpha = 0.18f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Shuffle, contentDescription = null, tint = PastelLavender, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shuffle", color = PastelLavender, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Track list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    itemsIndexed(tracks) { index, track ->
                        LocalFileTrackRow(
                            track = track,
                            index = index + 1,
                            onClick = {
                                viewModel.playTrack(track, tracks)
                                onOpenNowPlaying()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalFileTrackRow(
    track: Track,
    index: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track number
        Text(
            text = "$index",
            color = PastelTextSecondary.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.width(26.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Album art
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PastelCard),
            contentAlignment = Alignment.Center
        ) {
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = PastelLavender,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title + Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = PastelTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                color = PastelTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Duration
        val durationStr = formatDuration(track.durationMs.toLong())
        Text(
            text = durationStr,
            color = PastelTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Play button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PastelMint.copy(alpha = 0.15f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = PastelMint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

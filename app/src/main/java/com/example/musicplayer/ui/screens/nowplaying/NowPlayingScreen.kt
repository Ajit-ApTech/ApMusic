package com.example.musicplayer.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCard
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelLavender
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelRose
import com.example.musicplayer.ui.theme.PastelTextPrimary
import com.example.musicplayer.ui.theme.PastelTextSecondary

import androidx.compose.material.icons.filled.Tune
import com.example.musicplayer.ui.screens.settings.EqualizerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onDismiss: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.positionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val eqState by viewModel.eqState.collectAsState()

    var showQueueSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistTitle by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var isDraggingSlider by remember { mutableStateOf(false) }
    var draggedSliderValue by remember { mutableFloatStateOf(0f) }

    // Swipe-down-to-dismiss state
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val dismissThreshold = 80.dp.value * 3   // dismiss after ~240px drag down

    val track = currentTrack

    var isDownloaded by remember { mutableStateOf(false) }

    LaunchedEffect(track?.id) {
        track?.id?.let {
            isFavorite = viewModel.isFavorite(it)
            isDownloaded = viewModel.isTrackDownloaded(it)
        }
    }

    val sliderPosition = if (isDraggingSlider) {
        draggedSliderValue
    } else {
        if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffsetY > dismissThreshold) {
                            onDismiss()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = { dragOffsetY = 0f }
                ) { _, dragAmount ->
                    if (dragAmount > 0) {  // only track downward drags
                        dragOffsetY += dragAmount
                    }
                }
            },
        color = PastelDarkBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Drag handle pill — visual indicator to swipe down
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PastelTextSecondary.copy(alpha = 0.4f))
            )

            // Top Header: Down chevron, Header title, EQ & Queue buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = PastelTextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "NOW PLAYING",
                    color = PastelLavender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Row {
                    IconButton(onClick = { showEqualizerSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Equalizer",
                            tint = if (showEqualizerSheet) PastelMint else PastelTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(
                            Icons.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (showQueueSheet) PastelMint else PastelTextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Large Album Artwork with rounded corners and soft border
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
                    .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = PastelLavender.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(PastelCard)
                    .border(1.dp, PastelBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!track?.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = track?.artworkUrl,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = PastelLavender,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Bottom Controls Grouped Tightly
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Track Title, Artist, and Action Icons (Add to Playlist + Heart + Download)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track?.title ?: "Select a song",
                            color = PastelTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = track?.artist ?: "Music Player",
                            color = PastelTextSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (track != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    viewModel.toggleDownload(track, isDownloaded)
                                    val nextState = !isDownloaded
                                    isDownloaded = nextState
                                    Toast.makeText(
                                        context,
                                        if (nextState) "Downloading song for offline..." else "Removed offline song",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.Download,
                                    contentDescription = "Download",
                                    tint = if (isDownloaded) PastelMint else PastelTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { showPlaylistSheet = true }) {
                                Icon(
                                    imageVector = Icons.Filled.PlaylistAdd,
                                    contentDescription = "Add to Playlist",
                                    tint = PastelLavender,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite(track, isFavorite)
                                    isFavorite = !isFavorite
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) PastelRose else PastelTextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrubbable Progress Slider
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        isDraggingSlider = true
                        draggedSliderValue = it
                    },
                    onValueChangeFinished = {
                        val targetMs = (draggedSliderValue * durationMs).toLong()
                        viewModel.seekTo(targetMs)
                        isDraggingSlider = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = PastelMint,
                        activeTrackColor = PastelMint,
                        inactiveTrackColor = PastelBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Timestamps (Elapsed vs Duration)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentMs = if (isDraggingSlider) (draggedSliderValue * durationMs).toLong() else positionMs
                    Text(
                        text = formatTime(currentMs),
                        color = PastelTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatTime(durationMs),
                        color = PastelTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transport Controls: Shuffle, Prev, Play/Pause, Next, Repeat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) PastelMint else PastelTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Previous
                    IconButton(onClick = { viewModel.skipPrevious() }) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = PastelTextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play / Pause Outlined Ring Button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(PastelCardElevated)
                            .border(2.dp, PastelMint, CircleShape)
                            .shadow(16.dp, CircleShape, ambientColor = PastelMint.copy(alpha = 0.4f), spotColor = PastelMint.copy(alpha = 0.4f))
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = PastelMint,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Next
                    IconButton(onClick = { viewModel.skipNext() }) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = PastelTextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat
                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                        val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat
                        val repeatTint = if (repeatMode != Player.REPEAT_MODE_OFF) PastelMint else PastelTextSecondary
                        Icon(
                            repeatIcon,
                            contentDescription = "Repeat",
                            tint = repeatTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Queue Access Modal Sheet
        if (showQueueSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = sheetState,
                containerColor = PastelCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Up Next",
                            color = PastelTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${queue.size} songs",
                            color = PastelLavender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (queue.isEmpty()) {
                        Text(
                            text = "No tracks in queue",
                            color = PastelTextSecondary,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(queue, key = { index, item -> "${item.id}_$index" }) { index, item ->
                                val isCurrent = item.id == currentTrack?.id
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.playQueueIndex(index)
                                            showQueueSheet = false
                                        }
                                        .border(
                                            1.dp,
                                            if (isCurrent) PastelMint.copy(alpha = 0.5f) else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    color = if (isCurrent) PastelMint.copy(alpha = 0.12f) else PastelCardElevated
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!item.artworkUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = item.artworkUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(PastelCard),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.MusicNote,
                                                    contentDescription = null,
                                                    tint = PastelLavender,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                color = if (isCurrent) PastelMint else PastelTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.artist,
                                                color = PastelTextSecondary,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (isCurrent) {
                                            Icon(
                                                Icons.Filled.MusicNote,
                                                contentDescription = "Playing",
                                                tint = PastelMint,
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
        }

        // Add to Playlist Modal Sheet
        if (showPlaylistSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPlaylistSheet = false },
                containerColor = PastelCard
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
                        Text(
                            text = "Add to Playlist",
                            color = PastelTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showNewPlaylistDialog = true }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "New Playlist",
                                tint = PastelMint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (playlists.isEmpty()) {
                        Text(
                            text = "No playlists found. Tap + to create one!",
                            color = PastelTextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((playlists.size * 56).coerceAtMost(300).dp)
                        ) {
                            items(playlists, key = { it.id }) { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (track != null) {
                                                viewModel.addTrackToPlaylist(playlist.id, track)
                                                Toast.makeText(context, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
                                            }
                                            showPlaylistSheet = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = PastelLavender,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = playlist.name,
                                        color = PastelTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Create Playlist Dialog
        if (showNewPlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showNewPlaylistDialog = false },
                containerColor = PastelCardElevated,
                title = {
                    Text(
                        text = "New Playlist",
                        color = PastelTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newPlaylistTitle,
                        onValueChange = { newPlaylistTitle = it },
                        placeholder = { Text("Playlist name", color = PastelTextSecondary) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newPlaylistTitle.isNotBlank()) {
                                viewModel.createPlaylist(newPlaylistTitle.trim())
                                Toast.makeText(context, "Created ${newPlaylistTitle.trim()}", Toast.LENGTH_SHORT).show()
                                newPlaylistTitle = ""
                                showNewPlaylistDialog = false
                            }
                        }
                    ) {
                        Text("Create", color = PastelMint, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewPlaylistDialog = false }) {
                        Text("Cancel", color = PastelTextSecondary)
                    }
                }
            )
        }

        // Quick Equalizer Bottom Sheet
        if (showEqualizerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEqualizerSheet = false },
                containerColor = PastelDarkBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    EqualizerCard(
                        state = eqState,
                        onToggleEnabled = { viewModel.audioEffectManager.toggleEnabled(it) },
                        onSelectPreset = { viewModel.audioEffectManager.setPreset(it) },
                        onBandLevelChange = { index, level -> viewModel.audioEffectManager.setBandLevel(index, level) },
                        onBassBoostChange = { viewModel.audioEffectManager.setBassBoost(it) },
                        onLoudnessGainChange = { viewModel.audioEffectManager.setLoudnessGain(it) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

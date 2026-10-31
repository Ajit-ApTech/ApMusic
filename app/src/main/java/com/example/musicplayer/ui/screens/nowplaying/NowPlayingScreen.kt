package com.example.musicplayer.ui.screens.nowplaying

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Timer

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
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val sleepTimerRemaining by viewModel.sleepTimerRemainingMs.collectAsState()
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

    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.93f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "artworkScale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPlaying) 28.dp else 10.dp,
        label = "shadowElevation"
    )

    val favScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favScale"
    )

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
        Box(modifier = Modifier.fillMaxSize()) {
            // Atmospheric dynamic aurora glow behind artwork
            Box(
                modifier = Modifier
                    .size(380.dp)
                    .align(Alignment.Center)
                    .offset(y = (-55).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PastelMint.copy(alpha = 0.28f),
                                PastelLavender.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Drag handle pill
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 2.dp)
                        .size(width = 42.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PastelTextSecondary.copy(alpha = 0.35f))
                )

                // Top Header: Down chevron, Header title with quality pill, Quick action icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PastelCardElevated.copy(alpha = 0.8f))
                            .border(1.dp, PastelBorder, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = PastelTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            color = PastelLavender,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            color = PastelCardElevated.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, PastelBorder)
                        ) {
                            Text(
                                text = "320 KBPS • LOSSLESS",
                                color = PastelTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showSleepTimerDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimerRemaining != null) PastelMint else PastelTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(
                            onClick = { showEqualizerSheet = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Equalizer",
                                tint = if (showEqualizerSheet) PastelMint else PastelTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(
                            onClick = { showQueueSheet = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Filled.QueueMusic,
                                contentDescription = "Queue",
                                tint = if (showQueueSheet) PastelMint else PastelTextPrimary,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (track != null) {
                                    viewModel.loadLyrics(track.id)
                                    showLyricsSheet = true
                                }
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Filled.Lyrics,
                                contentDescription = "Lyrics",
                                tint = if (showLyricsSheet) PastelMint else PastelTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Large Album Artwork with dynamic scale, glowing shadow, and hairline border
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.84f)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = artworkScale
                            scaleY = artworkScale
                        }
                        .shadow(
                            elevation = shadowElevation,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = PastelMint.copy(alpha = 0.4f),
                            spotColor = PastelLavender.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(PastelCard)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp)),
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(PastelMint.copy(alpha = 0.3f), PastelLavender.copy(alpha = 0.2f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = PastelMint,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }

                // Bottom Controls Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    // Track Title, Artist, and Action Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track?.title ?: "Select a song",
                                color = PastelTextPrimary,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track?.artist ?: "Music Player",
                                color = PastelTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (track != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
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
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.Download,
                                        contentDescription = "Download",
                                        tint = if (isDownloaded) PastelMint else PastelTextSecondary,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { showPlaylistSheet = true },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlaylistAdd,
                                        contentDescription = "Add to Playlist",
                                        tint = PastelLavender,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.toggleFavorite(track, isFavorite)
                                        isFavorite = !isFavorite
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .graphicsLayer {
                                            scaleX = favScale
                                            scaleY = favScale
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFavorite) PastelRose else PastelTextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                            thumbColor = Color.White,
                            activeTrackColor = PastelMint,
                            inactiveTrackColor = PastelBorder.copy(alpha = 0.8f)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transport Controls: Shuffle, Prev, Hero Play/Pause, Next, Repeat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle with active dot indicator
                        IconButton(
                            onClick = { viewModel.toggleShuffle() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (isShuffle) PastelMint else PastelTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (isShuffle) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(PastelMint)
                                    )
                                }
                            }
                        }

                        // Previous
                        IconButton(
                            onClick = { viewModel.skipPrevious() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = PastelTextPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Luminous Hero Play / Pause Button with gradient fill & deep glow
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = CircleShape,
                                    ambientColor = PastelMint.copy(alpha = 0.6f),
                                    spotColor = PastelLavender.copy(alpha = 0.5f)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(PastelMint, PastelLavender)
                                    )
                                )
                                .clickable { viewModel.togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Next
                        IconButton(
                            onClick = { viewModel.skipNext() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                tint = PastelTextPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Repeat with active dot indicator
                        IconButton(
                            onClick = { viewModel.toggleRepeat() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            val isRepeatActive = repeatMode != Player.REPEAT_MODE_OFF
                            val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat
                            val repeatTint = if (isRepeatActive) PastelMint else PastelTextSecondary
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    repeatIcon,
                                    contentDescription = "Repeat",
                                    tint = repeatTint,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (isRepeatActive) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(PastelMint)
                                    )
                                }
                            }
                        }
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
                        IconButton(onClick = { viewModel.clearQueue() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Clear Queue", tint = PastelRose, modifier = Modifier.size(20.dp))
                        }
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
                                        IconButton(onClick = { viewModel.removeQueueItem(index) }) {
                                            Icon(
                                                Icons.Filled.Clear,
                                                contentDescription = "Remove",
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

        // Lyrics Bottom Sheet
        if (showLyricsSheet) {
            val lyrics by viewModel.lyrics.collectAsState()
            ModalBottomSheet(
                onDismissRequest = { showLyricsSheet = false },
                containerColor = PastelDarkBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lyrics",
                            color = PastelTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showLyricsSheet = false }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = PastelTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val currentLyrics = lyrics
                    if (currentLyrics.isNullOrBlank()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PastelLavender.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Lyrics,
                                    contentDescription = null,
                                    tint = PastelLavender,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No lyrics available for this track",
                                color = PastelTextSecondary,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, PastelBorder, RoundedCornerShape(16.dp)),
                            color = PastelCard
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(currentLyrics.split("\n")) { line ->
                                        Text(
                                            text = line,
                                            color = PastelTextPrimary,
                                            fontSize = 14.sp,
                                            lineHeight = 24.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
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
        if (showSleepTimerDialog) {
            val timerOptions = listOf(15, 30, 45, 60, 90, 120)
            AlertDialog(
                onDismissRequest = { showSleepTimerDialog = false },
                title = { Text("Sleep Timer", color = PastelTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        timerOptions.forEach { mins ->
                            TextButton(
                                onClick = {
                                    viewModel.startSleepTimer(mins * 60 * 1000L)
                                    showSleepTimerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("$mins minutes", color = PastelTextPrimary)
                            }
                        }
                        TextButton(
                            onClick = {
                                viewModel.cancelSleepTimer()
                                showSleepTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Turn Off", color = PastelRose)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSleepTimerDialog = false }) {
                        Text("Close", color = PastelMint)
                    }
                },
                containerColor = PastelCard,
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

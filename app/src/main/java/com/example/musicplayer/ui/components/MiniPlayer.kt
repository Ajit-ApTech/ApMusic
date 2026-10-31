package com.example.musicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import coil.compose.AsyncImage
import com.example.musicplayer.ui.screens.nowplaying.NowPlayingViewModel
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelLavender
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelRose
import com.example.musicplayer.ui.theme.PastelTextPrimary
import com.example.musicplayer.ui.theme.PastelTextSecondary

@Composable
fun MiniPlayer(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.positionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()

    AnimatedVisibility(
        visible = currentTrack != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        val track = currentTrack ?: return@AnimatedVisibility
        var isFav by remember(track.id) { mutableStateOf(false) }

        LaunchedEffect(track.id) {
            isFav = viewModel.isFavorite(track.id)
        }

        val favScale by animateFloatAsState(
            targetValue = if (isFav) 1.2f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "miniFavScale"
        )

        val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = PastelMint.copy(alpha = 0.25f),
                    spotColor = PastelLavender.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .clickable { onExpand() },
            color = PastelCardElevated.copy(alpha = 0.96f),
            tonalElevation = 10.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork Thumbnail with rounded corner and subtle border
                    if (!track.artworkUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = track.artworkUrl,
                            contentDescription = "Track Art",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(0.8.dp, PastelBorder, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(PastelMint.copy(alpha = 0.35f), PastelLavender.copy(alpha = 0.2f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = PastelMint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist
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
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Like Button with bounce scale
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(track, isFav)
                            isFav = !isFav
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .graphicsLayer {
                                scaleX = favScale
                                scaleY = favScale
                            }
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) PastelRose else PastelTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Tactile Play/Pause Hero Button with gradient fill
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = PastelMint.copy(alpha = 0.5f),
                                spotColor = PastelLavender.copy(alpha = 0.4f)
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Micro Progress Line along bottom edge
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = PastelMint,
                    trackColor = PastelBorder.copy(alpha = 0.4f)
                )
            }
        }
    }
}

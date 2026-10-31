package com.example.musicplayer.ui.screens.search

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
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

@Composable
fun SearchScreen(
    navController: NavController,
    onOpenNowPlaying: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val downloadedTracks by viewModel.downloadedTracks.collectAsState(initial = emptyList())
    val downloadStates by viewModel.downloadStates.collectAsState()
    val downloadedIds = downloadedTracks.map { it.id }.toSet()

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

            Text(
                text = "Search",
                color = PastelTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Box with rounded pill shape
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChanged(it) },
                placeholder = {
                    Text(
                        "Search songs, albums, artists...",
                        color = PastelTextSecondary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = PastelLavender
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = PastelTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PastelCard,
                    unfocusedContainerColor = PastelCard,
                    focusedTextColor = PastelTextPrimary,
                    unfocusedTextColor = PastelTextPrimary,
                    focusedBorderColor = PastelMint,
                    unfocusedBorderColor = PastelBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Active provider: ${viewModel.activeProviderName}",
                color = PastelLavender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PastelMint, strokeWidth = 2.5.dp)
                }
            } else if (query.isNotBlank() && tracks.isEmpty() && albums.isEmpty() && artists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No results found for \"$query\"",
                            color = PastelTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try searching by another title or switch providers in Settings",
                            color = PastelTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (query.isBlank()) {
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
                                .background(PastelCardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = PastelMint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Find Your Favorite Music",
                            color = PastelTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search songs, albums, and artists across providers",
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
                    // Albums Section in Search
                    if (albums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Albums",
                                color = PastelTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(albums, key = { "search_album_${it.id}" }) { album ->
                                    SearchAlbumCard(album = album, onClick = {
                                        navController.navigate("album/${album.id}")
                                    })
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // Artists Section in Search
                    if (artists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Artists",
                                color = PastelTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(artists, key = { "search_artist_${it.id}" }) { artist ->
                                    SearchArtistCard(artist = artist, onClick = {
                                        navController.navigate("artist/${artist.id}")
                                    })
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    // Songs Section in Search
                    if (tracks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Songs",
                                color = PastelTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(tracks, key = { "search_track_${it.id}" }) { track ->
                            val isDownloaded = downloadedIds.contains(track.id)
                            val state = downloadStates[track.id]
                            TrackRowItem(
                                track = track,
                                modifier = Modifier.padding(vertical = 4.dp),
                                isDownloaded = isDownloaded,
                                downloadState = state,
                                onDownloadClick = { viewModel.downloadTrack(track) },
                                onClick = {
                                    viewModel.playTrack(track)
                                    onOpenNowPlaying()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchAlbumCard(album: Album, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = PastelCard
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = album.artworkUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = album.name,
                color = PastelTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = album.artist,
                color = PastelTextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SearchArtistCard(artist: Artist, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = PastelCard
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = artist.artworkUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = artist.name,
                color = PastelTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

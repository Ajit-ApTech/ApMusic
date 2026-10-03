package com.example.musicplayer.ui.screens.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun PlaylistsScreen(navController: NavController) {
    val viewModel: PlaylistsViewModel = hiltViewModel()
    val playlists by viewModel.playlists.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Playlists", style = MaterialTheme.typography.titleLarge)
        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(playlists, key = { it.id }) { playlist ->
                Text(
                    text = "${playlist.name} (${playlist.id})",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

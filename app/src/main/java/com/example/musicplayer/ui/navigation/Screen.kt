package com.example.musicplayer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object Library : Screen("library", "Library", Icons.Filled.LibraryMusic)
    object Files : Screen("files", "My Files", Icons.Filled.FolderOpen)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object NowPlaying : Screen("nowplaying", "Now Playing", Icons.Filled.Home)
}

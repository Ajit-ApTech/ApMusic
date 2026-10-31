package com.example.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.musicplayer.ui.screens.album.AlbumDetailScreen
import com.example.musicplayer.ui.screens.artist.ArtistDetailScreen
import com.example.musicplayer.ui.screens.home.HomeScreen
import com.example.musicplayer.ui.screens.library.LibraryScreen
import com.example.musicplayer.ui.screens.localfiles.LocalFilesScreen
import com.example.musicplayer.ui.screens.search.SearchScreen
import com.example.musicplayer.ui.screens.settings.SettingsScreen
import com.example.musicplayer.ui.screens.nowplaying.NowPlayingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onOpenNowPlaying: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, onOpenNowPlaying = onOpenNowPlaying)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController, onOpenNowPlaying = onOpenNowPlaying)
        }
        composable(
            route = "${Screen.Library.route}?tab={tab}",
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            LibraryScreen(
                navController = navController,
                initialTab = initialTab,
                onOpenNowPlaying = onOpenNowPlaying
            )
        }
        composable(Screen.Files.route) {
            LocalFilesScreen(onOpenNowPlaying = onOpenNowPlaying)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(
            route = "album/{albumId}",
            arguments = listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
            AlbumDetailScreen(
                albumId = albumId,
                navController = navController,
                onOpenNowPlaying = onOpenNowPlaying
            )
        }
        composable(
            route = "artist/{artistId}",
            arguments = listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
            ArtistDetailScreen(
                artistId = artistId,
                navController = navController,
                onOpenNowPlaying = onOpenNowPlaying
            )
        }
    }
}
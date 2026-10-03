package com.example.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.musicplayer.ui.screens.home.HomeScreen
import com.example.musicplayer.ui.screens.library.LibraryScreen
import com.example.musicplayer.ui.screens.localfiles.LocalFilesScreen
import com.example.musicplayer.ui.screens.search.SearchScreen
import com.example.musicplayer.ui.screens.settings.SettingsScreen

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
    }
}

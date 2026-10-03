package com.example.musicplayer.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.ui.components.MiniPlayer
import com.example.musicplayer.ui.screens.nowplaying.NowPlayingScreen
import com.example.musicplayer.ui.screens.nowplaying.NowPlayingViewModel
import com.example.musicplayer.ui.theme.PastelBorder
import com.example.musicplayer.ui.theme.PastelCardElevated
import com.example.musicplayer.ui.theme.PastelDarkBg
import com.example.musicplayer.ui.theme.PastelMint
import com.example.musicplayer.ui.theme.PastelTextSecondary

@Composable
fun MainScreen(
    nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = isNowPlayingExpanded) {
        isNowPlayingExpanded = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PastelDarkBg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = PastelDarkBg,
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PastelDarkBg)
                    ) {
                        MiniPlayer(
                            onExpand = { isNowPlayingExpanded = true },
                            viewModel = nowPlayingViewModel
                        )
                        PastelBottomBar(navController = navController)
                    }
                }
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                    onOpenNowPlaying = { isNowPlayingExpanded = true }
                )
            }

            // Fullscreen Now Playing Overlay
            AnimatedVisibility(
                visible = isNowPlayingExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                NowPlayingScreen(
                    onDismiss = { isNowPlayingExpanded = false },
                    viewModel = nowPlayingViewModel
                )
            }
        }
    }
}

@Composable
fun PastelBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library,
        Screen.Files,
        Screen.Settings
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, PastelBorder, RoundedCornerShape(32.dp)),
        color = PastelCardElevated,
        tonalElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val isSelected = currentRoute == screen.route || currentRoute?.startsWith("${screen.route}?") == true

                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) PastelMint.copy(alpha = 0.22f) else Color.Transparent,
                    label = "tabBg"
                )
                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) PastelMint else PastelTextSecondary,
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(animatedBgColor)
                        .clickable {
                            if (!isSelected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            tint = animatedContentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = screen.label,
                                color = animatedContentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

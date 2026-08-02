package com.example.leitner.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.leitner.ui.cards.CardListScreen
import com.example.leitner.ui.cards.GithubDeckLibraryScreen
import com.example.leitner.ui.dashboard.DashboardScreen
import com.example.leitner.ui.review.ReviewScreen
import com.example.leitner.ui.settings.SettingsScreen

@Composable
fun LeitnerNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    Scaffold(bottomBar = {
        if (currentRoute != "github-library" && currentRoute != "review") {
            NavigationBar {
                val items = listOf("dashboard" to ("首頁" to Icons.Rounded.Home), "cards" to ("卡片" to Icons.Rounded.List), "settings" to ("設定" to Icons.Rounded.Settings))
                items.forEach { (route, item) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = { navController.navigate(route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) }
                    )
                }
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = "dashboard", modifier = androidx.compose.ui.Modifier.padding(padding)) {
            composable("dashboard") { DashboardScreen(onReview = { navController.navigate("review") }, onAddCard = { navController.navigate("cards") }) }
            composable("cards") { CardListScreen(onOpenGithubLibrary = { navController.navigate("github-library") }) }
            composable("github-library") { GithubDeckLibraryScreen(onBack = { navController.popBackStack() }) }
            composable("review") { ReviewScreen(onClose = { navController.popBackStack() }) }
            composable("settings") { SettingsScreen() }
        }
    }
}

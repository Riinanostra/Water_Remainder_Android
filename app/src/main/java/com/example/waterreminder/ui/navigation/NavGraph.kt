package com.example.waterreminder.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.waterreminder.ui.components.BottomNavigationBar
import com.example.waterreminder.ui.goals.GoalsScreen
import com.example.waterreminder.ui.goals.GoalsViewModel
import com.example.waterreminder.ui.history.HistoryScreen
import com.example.waterreminder.ui.history.HistoryViewModel
import com.example.waterreminder.ui.home.HomeScreen
import com.example.waterreminder.ui.home.HomeViewModel
import com.example.waterreminder.ui.profile.ProfileScreen
import com.example.waterreminder.ui.profile.ProfileViewModel
import com.example.waterreminder.ui.reminders.RemindersScreen
import com.example.waterreminder.ui.reminders.RemindersViewModel
import com.example.waterreminder.data.datastore.PreferencesManager

@Composable
fun NavGraph(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    homeViewModel: HomeViewModel,
    remindersViewModel: RemindersViewModel,
    goalsViewModel: GoalsViewModel,
    historyViewModel: HistoryViewModel,
    profileViewModel: ProfileViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomRoutes = setOf(
        Screen.Home.route,
        Screen.Reminders.route,
        Screen.Goals.route,
        Screen.History.route,
        Screen.Profile.route
    )
    val lastTabRoute by preferencesManager.lastTabRoute.collectAsState(initial = Screen.Home.route)
    val startDestination = if (lastTabRoute in bottomRoutes) lastTabRoute else Screen.Home.route

    LaunchedEffect(currentRoute) {
        if (currentRoute != null && currentRoute in bottomRoutes) {
            preferencesManager.setLastTabRoute(currentRoute)
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) { HomeScreen(homeViewModel, preferencesManager) }
            composable(Screen.Reminders.route) { RemindersScreen(remindersViewModel, preferencesManager) }
            composable(Screen.Goals.route) { GoalsScreen(goalsViewModel, preferencesManager) }
            composable(Screen.History.route) { HistoryScreen(historyViewModel, preferencesManager) }
            composable(Screen.Profile.route) { ProfileScreen(profileViewModel, preferencesManager) }
        }
    }
}

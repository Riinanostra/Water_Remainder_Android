package com.example.waterreminder.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Outlined.Home)
    data object Reminders : Screen("reminders", "Reminders", Icons.Outlined.Notifications)
    data object Goals : Screen("goals", "Goals", Icons.Outlined.Tune)
    data object History : Screen("history", "History", Icons.Outlined.BarChart)
    data object Profile : Screen("profile", "More", Icons.Outlined.MoreHoriz)
}

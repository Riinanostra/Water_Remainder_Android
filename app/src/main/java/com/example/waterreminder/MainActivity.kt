package com.example.waterreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.data.local.WaterDatabase
import com.example.waterreminder.data.repository.GoalRepository
import com.example.waterreminder.data.repository.ReminderRepository
import com.example.waterreminder.data.repository.SyncRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.domain.usecase.LogWaterUseCase
import com.example.waterreminder.domain.usecase.ObserveGoalConfigUseCase
import com.example.waterreminder.domain.usecase.ObserveHistoryUseCase
import com.example.waterreminder.domain.usecase.ObserveReminderConfigUseCase
import com.example.waterreminder.domain.usecase.ObserveTodayIntakeUseCase
import com.example.waterreminder.ui.goals.GoalsViewModel
import com.example.waterreminder.ui.history.HistoryViewModel
import com.example.waterreminder.ui.home.HomeViewModel
import com.example.waterreminder.ui.navigation.NavGraph
import com.example.waterreminder.ui.profile.ProfileViewModel
import com.example.waterreminder.ui.reminders.RemindersViewModel
import com.example.waterreminder.ui.theme.WaterReminderTheme
import com.example.waterreminder.util.ThemeMode
import com.example.waterreminder.worker.SyncScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncScheduler.schedule(this)
        val database = WaterDatabase.getInstance(this)
        val waterRepository = WaterRepository(database.waterIntakeDao())
        val reminderRepository = ReminderRepository(database.reminderConfigDao())
        val goalRepository = GoalRepository(database.goalConfigDao())
        val preferencesManager = PreferencesManager(this)
        val syncRepository = SyncRepository(
            context = this,
            preferencesManager = preferencesManager,
            goalRepository = goalRepository,
            waterRepository = waterRepository
        )

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            WaterReminderTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        this,
                        LogWaterUseCase(waterRepository),
                        ObserveTodayIntakeUseCase(waterRepository),
                        ObserveGoalConfigUseCase(goalRepository),
                        ObserveReminderConfigUseCase(reminderRepository),
                        ObserveHistoryUseCase(waterRepository)
                    )
                )
                val remindersViewModel: RemindersViewModel = viewModel(
                    factory = RemindersViewModel.Factory(
                        context = this,
                        reminderRepository = reminderRepository,
                        preferencesManager = preferencesManager
                    )
                )
                val goalsViewModel: GoalsViewModel = viewModel(
                    factory = GoalsViewModel.Factory(goalRepository, preferencesManager)
                )
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(
                        ObserveHistoryUseCase(waterRepository),
                        ObserveGoalConfigUseCase(goalRepository),
                        preferencesManager,
                        syncRepository
                    )
                )
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(this, preferencesManager, waterRepository, syncRepository)
                )
                NavGraph(
                    navController = navController,
                    preferencesManager = preferencesManager,
                    homeViewModel = homeViewModel,
                    remindersViewModel = remindersViewModel,
                    goalsViewModel = goalsViewModel,
                    historyViewModel = historyViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}

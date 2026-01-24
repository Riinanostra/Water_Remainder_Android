package com.example.waterreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.data.local.WaterDatabase
import com.example.waterreminder.data.repository.GoalRepository
import com.example.waterreminder.data.repository.SyncRepository
import com.example.waterreminder.data.repository.WaterRepository

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = WaterDatabase.getInstance(applicationContext)
        val waterRepository = WaterRepository(database.waterIntakeDao())
        val goalRepository = GoalRepository(database.goalConfigDao())
        val preferencesManager = PreferencesManager(applicationContext)
        val syncRepository = SyncRepository(
            context = applicationContext,
            preferencesManager = preferencesManager,
            goalRepository = goalRepository,
            waterRepository = waterRepository
        )

        val historyResult = syncRepository.uploadHistory()
        val deviceResult = syncRepository.uploadDeviceInfo()

        return if (historyResult.isSuccess && deviceResult.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}

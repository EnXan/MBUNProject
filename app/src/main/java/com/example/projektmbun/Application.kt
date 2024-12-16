package com.example.projektmbun

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projektmbun.models.cloud.listener.RecipeListener
import com.example.projektmbun.workers.RoutineExecutionWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit

class ZeroWasteCook : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val recipeListener = RecipeListener(context = applicationContext)

        recipeListener.listenToFoodChanges(applicationScope)

        // Sofortige Ausführung
        val immediateWorkRequest = OneTimeWorkRequestBuilder<RoutineExecutionWorker>().build()
        WorkManager.getInstance(this).enqueue(immediateWorkRequest)

        // Planung für tägliche Wiederholung
        val periodicWorkRequest = PeriodicWorkRequestBuilder<RoutineExecutionWorker>(
            1, TimeUnit.DAYS // Wiederholungsintervall
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RoutineCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}
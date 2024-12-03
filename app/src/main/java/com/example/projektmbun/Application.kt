package com.example.projektmbun

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projektmbun.workers.RoutineExecutionWorker
import java.util.concurrent.TimeUnit

class ZeroWasteCook : Application() {

    override fun onCreate() {
        super.onCreate()

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
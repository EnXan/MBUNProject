package com.example.projektmbun

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.example.projektmbun.models.cloud.listener.FoodListener
import com.example.projektmbun.workers.RoutineExecutionWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ZeroWasteCook : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val foodListener = FoodListener(context = applicationContext)
        foodListener.listenToFoodChanges(applicationScope)

        TransferNetworkLossHandler.getInstance(applicationContext)

        applicationScope.launch {
            foodListener.initializeApp(applicationScope)
        }

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
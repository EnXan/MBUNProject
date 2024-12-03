package com.example.projektmbun.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.models.database.AppDatabase
import java.io.IOException

class RoutineExecutionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // Erstelle eine Instanz des RoutineControllers
    private val routineController: RoutineController

    init {
        val database = AppDatabase.getDatabase(context) // Zugriff auf die Datenbank
        val routineDao = database.routineDao()
        val foodCardDao = database.foodCardDao()
        routineController = RoutineController(routineDao, foodCardDao) // RoutineController erstellen
    }

    override suspend fun doWork(): Result {
        return try {
            val activeRoutines = routineController.getAllActiveRoutines()
            activeRoutines.forEach { routine ->
                routineController.executeRoutineIfDueToday(routine)
            }
            Log.d("RoutineExecutionWorker", "Alle aktiven Routinen erfolgreich ausgeführt")
            Result.success()
        } catch (e: IOException) {
            Log.e("RoutineExecutionWorker", "Netzwerkfehler: ${e.message}")
            Result.retry()
        } catch (e: Exception) {
            Log.e("RoutineExecutionWorker", "Unerwarteter Fehler: ${e.message}")
            Result.failure()
        }
    }


}

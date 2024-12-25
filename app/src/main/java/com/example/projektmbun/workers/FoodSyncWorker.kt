package com.example.projektmbun.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.models.database.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodSyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val foodDao = AppDatabase.getDatabase(context).foodDao()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) { // Wechsel zum IO-Thread
            try {
                val foodList = supabase.from("food")
                    .select(columns = Columns.list("name", "category"))
                    .decodeList<FoodLocal>()

                if (foodList.isNotEmpty()) {
                    foodDao.insertAllFood(foodList)
                    Result.success()
                } else {
                    Result.success() // Erfolg, auch wenn keine neuen Daten vorhanden sind
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry() // Bei Fehler erneut versuchen
            }
        }
    }
}

package com.example.projektmbun.models.cloud.listener

import android.content.Context
import android.util.Log
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.models.database.supabase
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.FoodStateEnum
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject

class RecipeListener(private val context: Context) {

    private val recipeService: RecipeService = RecipeService()
    private val foodDao = AppDatabase.getDatabase(context).foodDao()

    fun listenToFoodChanges(scope: CoroutineScope) {
        scope.launch {
            val foodChannel: RealtimeChannel = supabase.channel("public:food")

            try {
                supabase.realtime.connect()

                val changes = foodChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "food"
                }

                changes.onEach { change ->
                    when (change) {
                        is PostgresAction.Insert -> handleFoodInsert(change.record)
                        is PostgresAction.Update -> handleFoodUpdate(change.record)
                        is PostgresAction.Delete -> handleFoodDelete(change.oldRecord)
                        else -> Log.d("RecipeListener", "Unbekannte Aktion: $change")
                    }
                }.launchIn(scope)

                foodChannel.subscribe()
                Log.d("RecipeListener", "Erfolgreich auf Rezeptänderungen abonniert")
            } catch (e: Exception) {
                Log.e("RecipeListener", "Fehler beim Abonnieren der Rezeptänderungen: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleFoodInsert(newFood: Map<String, Any>) {
        Log.d("FoodListener", "Received new food data: $newFood")
        try {
            Log.d("RecipeListener", "Received new food data: $newFood")
            val jsonObject = JSONObject(newFood.toString()) // Falls es kein reines JSONObject ist
            val categoryString = jsonObject.optString("category", null)
            Log.d("RecipeListener", "Extracted Category String: $categoryString")
            val foodName = jsonObject.optString("name", null)
            Log.d("RecipeListener", "Extracted Name String: $foodName")
            val stateString = jsonObject.optString("state", null)
            Log.d("RecipeListener", "Extracted State String: $stateString")

            // Konvertiere die Strings zu Enums
            val category = categoryString?.let { FoodCategoryEnum.valueOf(it) } ?: FoodCategoryEnum.UNBEKANNT
            val state = stateString?.let { FoodStateEnum.valueOf(it) } ?: FoodStateEnum.UNBEKANNT

            if (foodName.isNullOrBlank()) {
                Log.e("FoodListener", "Food name is null or empty. Skipping insert.")
                return
            }

            val food = FoodLocal(
                name = foodName,
                category = category, // Enum wird als String gespeichert
                state = state
            )

            foodDao.insertFood(food)
            Log.d("FoodListener", "Erfolgreich Essen eingefügt: $food")
            val foods = foodDao.getAllFood()
            Log.d("DB_QUERY", "Fetched foods: $foods")
        } catch (e: Exception) {
            Log.e("FoodListener", "Fehler beim Einfügen des Essens: ${e.message}")
            e.printStackTrace()
        }
    }


    private suspend fun handleFoodUpdate(updatedFood: Map<String, Any?>) {
        Log.d("FoodListener", "Received updated food data: $updatedFood")
        try {
            val jsonObject = JSONObject(updatedFood.toString())
            val categoryString = jsonObject.optString("category", null)
            val foodName = jsonObject.optString("name", null)
            val stateString = jsonObject.optString("state", null)

            val category = categoryString?.let { FoodCategoryEnum.valueOf(it) } ?: FoodCategoryEnum.UNBEKANNT
            val state = stateString?.let { FoodStateEnum.valueOf(it) } ?: FoodStateEnum.UNBEKANNT

            if (foodName.isNullOrBlank()) {
                Log.e("FoodListener", "Food name is null or empty. Skipping update.")
                return
            }

            val food = FoodLocal(
                name = foodName,
                category = category,
                state = state
            )

            foodDao.updateFood(food)
            Log.d("FoodListener", "Erfolgreich Essen aktualisiert: $food")
        } catch (e: Exception) {
            Log.e("FoodListener", "Fehler beim Aktualisieren des Essens: ${e.message}")
            e.printStackTrace()
        }
    }


    private suspend fun handleFoodDelete(oldFood: Map<String, Any?>) {
        Log.d("FoodListener", "Received food data to delete: $oldFood")
        try {
            val jsonObject = JSONObject(oldFood.toString())
            val foodName = jsonObject.optString("name", null)

            if (foodName.isNullOrBlank()) {
                Log.e("FoodListener", "Food name is null or empty. Skipping delete.")
                return
            }

            foodDao.deleteFoodByName(foodName)
            Log.d("FoodListener", "Erfolgreich Essen mit Namen gelöscht: $foodName")
        } catch (e: Exception) {
            Log.e("FoodListener", "Fehler beim Löschen des Essens: ${e.message}")
            e.printStackTrace()
        }
    }

}

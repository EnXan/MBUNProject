package com.example.projektmbun.models.cloud.service

import android.util.Log
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.database.supabase
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodService {

    private val foodTable = "food"

    suspend fun createFood(foodLocal: FoodLocal): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).insert(foodLocal)
            true
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun getFoodByName(name: String): List<FoodLocal> = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).select {
                filter {
                    eq("name", name)
                }
            }.decodeList<FoodLocal>()
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getAllFood(): List<FoodLocal> = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).select().decodeList<FoodLocal>()
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getFoodByCategory(foodCategory: FoodCategoryEnum): List<FoodLocal> = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).select {
                filter {
                    eq("category", foodCategory.name)
                }
            }.decodeList<FoodLocal>()
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getFoodsByNames(names: List<String>): List<FoodLocal> = withContext(Dispatchers.IO) {
        try {
            supabase.from("food").select {
                filter {
                    isIn("name", names)
                }
            }.decodeList<FoodLocal>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}
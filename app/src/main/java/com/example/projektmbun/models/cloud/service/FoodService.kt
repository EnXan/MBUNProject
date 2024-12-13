package com.example.projektmbun.models.cloud.service

import android.util.Log
import com.example.projektmbun.models.data_structure.food.Food
import com.example.projektmbun.models.database.supabase
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodService {

    private val foodTable = "food"

    suspend fun createFood(food: Food): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).insert(food)
            true
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            false
        }
    }

    suspend fun getFoodByName(name: String): List<Food> = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).select {
                filter {
                    eq("name", name)
                }
            }.decodeList<Food>()
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getAllFood(): List<Food> = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).select().decodeList<Food>()
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getFoodByCategory(foodCategory: FoodCategoryEnum): List<Food> = withContext(Dispatchers.IO) {
        try {
            supabase.from(foodTable).select {
                filter {
                    eq("category", foodCategory.name)
                }
            }.decodeList<Food>()
        } catch (e: Exception) {
            Log.e("SupabaseFoodService", "Unexpected error: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getFoodsByNames(names: List<String>): List<Food> = withContext(Dispatchers.IO) {
        try {
            supabase.from("food").select {
                filter {
                    isIn("name", names)
                }
            }.decodeList<Food>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}
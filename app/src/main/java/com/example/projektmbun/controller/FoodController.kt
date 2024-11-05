package com.example.projektmbun.controller

import android.content.Context
import android.util.Log
import com.example.projektmbun.models.daos.FoodDao
import com.example.projektmbun.models.data.Food
import com.example.projektmbun.models.database.FoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext

class FoodController(private val foodDao: FoodDao) {

    /**
     * Add or update a new food entry to the database.
     * @param food name of the related food.
     */
    suspend fun addFood(food: Food) {
        if (food.food.isEmpty() || food.food.isBlank()) {
            throw IllegalArgumentException("food name can't be empty or blank!")
        }
        withContext(Dispatchers.IO) {
            try {
                foodDao.createOrReplaceFood(food)
            } catch (e: Exception) {
                Log.e("FoodController", "Error adding or updating food: ${e.message}")
                // Optionally, throw a custom exception or notify the UI of the error
                throw e
            }
        }
    }

    /**
     * Deletes a food entry from the database by its name.
     * @param food name of the food.
     */
    suspend fun deleteFood(food: Food) {
        withContext(Dispatchers.IO) {
            try {
                foodDao.deleteFood(food)
            } catch (e: Exception) {
                Log.e("FoodController", "Error deleting food: ${e.message}")
                // Optionally, throw a custom exception or notify the UI of the error
                throw e
            }
        }
    }

    /**
     * Get food by its name.
     * @param name of the food.
     * @return Flow list of all found foods.
     */
    fun getFood(name: String): Flow<List<Food>> {
        return try {
            foodDao.getFoodsByName(name)
        } catch (e: Exception) {
            Log.e("FoodController", "Error fetching food by name: ${e.message}")
            // Return an empty flow if there's an error
            emptyFlow()
        }
    }

    /**
     * Get all foods.
     * @return Flow list of all foods.
     */
    fun getAllFood(): Flow<List<Food>> {
        return try {
            foodDao.getAllFood()
        } catch (e: Exception) {
            Log.e("FoodController", "Error fetching all foods: ${e.message}")
            emptyFlow()
        }
    }

    /**
     * Get food by its category.
     * @param category of the food.
     * @return Flow list of all found foods.
     */
    fun getFoodByCategory(category: String): Flow<List<Food>> {
        return try {
            foodDao.getFoodsByCategory(category)
        } catch (e: Exception) {
            Log.e("FoodController", "Error fetching food by category: ${e.message}")
            emptyFlow()
        }
    }

    /**
     * get food by search and selected category
     * @param query as string
     * @param category selected
     * @return Flow list of all found foods.
     */
    suspend fun getFilteredFood(query: String, category: String?): Flow<List<Food>> {
        // Implement logic to filter by both query and category
        return if (category != null && query.isNotEmpty()) {
            foodDao.getFoodsByCategoryAndQuery(category, query)
        } else if (category != null) {
            foodDao.getFoodsByCategory(category)
        } else if (query.isNotEmpty()) {
            foodDao.getFoodsByName(query)
        } else {
            foodDao.getAllFood()
        }
    }

}

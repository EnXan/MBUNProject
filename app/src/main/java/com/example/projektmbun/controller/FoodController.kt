package com.example.projektmbun.controller

import android.util.Log
import com.example.projektmbun.models.daos.FoodDao
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.SearchLogic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Controller class responsible for managing `Food` operations, including
 * fetching food items by name, category, or retrieving all food items.
 * It interacts with the `FoodDao` to perform database operations and provides
 * business logic for handling food-related functionality.
 *
 * @property foodDao DAO for performing `Food` operations.
 */
class FoodController(private val foodDao: FoodDao) {

    /**
     * Get food by their name with fuzzy search..
     * @param name of the food.
     * @return List of all found food or empty list `List<Food>`.
     */
    suspend fun getFoodByName(name: String): List<Food> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        try {
            val allFood = foodDao.getAllFood()

            if (trimmedName.isBlank()) {
                return@withContext allFood
            }

            //call Fuzzy-Search if name not empty
            SearchLogic.fuzzySearch(
                query = trimmedName,
                items = allFood,
                nameSelector = { food -> food.name },
                threshold = -3
            )
        } catch (e: Exception) {
            Log.e("FoodController", "Error fetching food by name: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get all foods.
     * @return List of all food or empty list `List<Food>`.
     */
    suspend fun getAllFood(): List<Food> {
        return withContext(Dispatchers.IO) {
            try {
                foodDao.getAllFood()
            }
            catch (e: Exception) {
            Log.e("FoodController", "Error fetching all foods: ${e.message}")
            emptyList()
        }
        }
    }

    /**
     * Get food by their category.
     * @param category of the food.
     * @return List of all found food or empty list `List<Food>`.
     */
    suspend fun getFoodByCategory(category: FoodCategoryEnum): List<Food> {
        return withContext(Dispatchers.IO) {
            try {
                foodDao.getFoodByCategory(category)
            }
            catch (e: Exception) {
                Log.e("FoodController", "Error fetching food by category: ${e.message}")
                emptyList()
            }
        }
    }
}

package com.example.projektmbun.controller

import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.food.Food
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodController(private val foodService: FoodService) {

    /**
     * Create a new food entry.
     * @param food The food item to create.
     * @return True if the operation succeeded, false otherwise.
     */
    suspend fun createFood(food: Food): Boolean = withContext(Dispatchers.IO) {
        if (food.name.isBlank()) {
            throw IllegalArgumentException("Food name cannot be empty.")
        }

        try {
            foodService.createFood(food)
        } catch (e: Exception) {
            throw Exception("Failed to create food: ${e.localizedMessage}")
        }
    }

    /**
     * Fetch a list of food items by name.
     * @param name The name of the food item to search for.
     * @return A list of matching food items or an empty list if none are found.
     */
    suspend fun searchFoodByName(name: String): List<Food> = withContext(Dispatchers.IO) {
        if (name.isBlank()) {
            return@withContext emptyList()
        }

        try {
            foodService.getFoodByName(name)
        } catch (e: Exception) {
            throw Exception("Failed to fetch food by name: ${e.localizedMessage}")
        }
    }

    /**
     * Fetch all food items.
     * @return A list of all food items or an empty list if none are found.
     */
    suspend fun getAllFood(): List<Food> = withContext(Dispatchers.IO) {
        try {
            foodService.getAllFood()
        } catch (e: Exception) {
            throw Exception("Failed to fetch all food items: ${e.localizedMessage}")
        }
    }

    /**
     * Fetch food items by category.
     * @param category The food category to filter by.
     * @return A list of matching food items or an empty list if none are found.
     */
    suspend fun getFoodByCategory(category: FoodCategoryEnum): List<Food> = withContext(Dispatchers.IO) {
        try {
            foodService.getFoodByCategory(category)
        } catch (e: Exception) {
            throw Exception("Failed to fetch food by category: ${e.localizedMessage}")
        }
    }
}

package com.example.projektmbun.controller

import com.example.projektmbun.controller.interfaces.IFoodController
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.local.daos.FoodDao
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FoodController(private val foodService: FoodService, private val foodDao: FoodDao): IFoodController {




    /**
     * Fetch a list of food items by name.
     * @param name The name of the food item to search for.
     * @return A list of matching food items or an empty list if none are found.
     */
    override suspend fun searchFoodByName(name: String): List<FoodLocal> = withContext(Dispatchers.IO) {
        if (name.isBlank()) {
            return@withContext emptyList()
        }

        try {
            foodDao.getFoodByName(name)
        } catch (e: Exception) {
            throw Exception("Failed to fetch food by name: ${e.localizedMessage}")
        }
    }

    /**
     * Fetch all food items.
     * @return A list of all food items or an empty list if none are found.
     */
    override suspend fun getAllFood(): Flow<List<FoodLocal>> = withContext(Dispatchers.IO) {
        try {
            foodDao.getAllFood()
        } catch (e: Exception) {
            throw Exception("Failed to fetch all food items: ${e.localizedMessage}")
        }
    }

    /**
     * Fetch food items by category.
     * @param category The food category to filter by.
     * @return A list of matching food items or an empty list if none are found.
     */
    override suspend fun getFoodByCategory(category: FoodCategoryEnum): List<FoodLocal> = withContext(Dispatchers.IO) {
            try {
                foodDao.getFoodByCategory(category)
            } catch (e: Exception) {
                throw Exception("Failed to fetch food by category: ${e.localizedMessage}")
            }
    }
}

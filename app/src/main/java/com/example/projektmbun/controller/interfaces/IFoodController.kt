package com.example.projektmbun.controller.interfaces

import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import kotlinx.coroutines.flow.Flow

interface IFoodController {
    suspend fun searchFoodByName(name: String): List<FoodLocal>
    suspend fun getAllFood(): Flow<List<FoodLocal>>
    suspend fun getFoodByCategory(category: FoodCategoryEnum): List<FoodLocal>
} 
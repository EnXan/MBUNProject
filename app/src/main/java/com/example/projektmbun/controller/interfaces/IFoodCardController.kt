package com.example.projektmbun.controller.interfaces

import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails

interface IFoodCardController {
    suspend fun addFoodCard(foodCard: FoodCard): Int
    suspend fun getFoodCardsWithDetails(): List<FoodCardWithDetails>
    suspend fun getFoodCardsInStock(): List<FoodCardWithDetails>
    suspend fun getFoodCardsByRoutineId(routineId: Int): List<FoodCardWithDetails>
    suspend fun deleteFoodCardById(foodCardId: Int)
    suspend fun addFoodCardToStock(foodCard: FoodCard, stockId: Int?): Long?
    suspend fun updateExpiryDateByFoodCardId(foodCardId: Int, newExpiryDate: String)
    suspend fun getFoodCardsByName(query: String): List<FoodCardWithDetails>
    suspend fun removeFoodCardFromStock(foodCardId: Int)
} 
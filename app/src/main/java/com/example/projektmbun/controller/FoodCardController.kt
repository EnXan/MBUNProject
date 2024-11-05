package com.example.projektmbun.controller

import android.content.Context
import android.util.Log
import com.example.projektmbun.models.data.FoodCard
import com.example.projektmbun.models.database.FoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Controller class for handling operations on FoodCard entries in the database. It interacts with
 * FoodCardDao. It uses coroutines to handle database operations on a background thread, ensuring
 * they don't block the main thread.
 * @param context Used to get an Instance of FoodDatabase.
 */
class FoodCardController(context: Context) {

    //used to perform database operations
    private val foodCardDao = FoodDatabase.getDatabase(context).foodCardDao()


    /**
     * Adds a new FoodCard entry to the database.
     * @param foodId name of the related food.
     * @param quantity of food.
     * @param expiringDate of food.
     */
    suspend fun addFoodCard(foodId: String, quantity: Int, expiringDate: LocalDate) {
        //Prevents UI freezes and increase performance
        withContext(Dispatchers.IO) {
            try {
                val foodCard = FoodCard(
                    foodId = foodId,
                    quantity = quantity,
                    expiringDate = expiringDate
                )
                foodCardDao.insertFoodCard(foodCard)
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error adding FoodCard: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Deletes a FoodCard entry from the database by id.
     * @param id of the foodCard.
     */
    suspend fun deleteFoodCardById(id: Int) {
        withContext(Dispatchers.IO) {
            try {
                foodCardDao.deleteFoodCardById(id)
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error deleting FoodCard by ID: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Get all foodCard entries from the database.
     */
    suspend fun getAllFoodCards(): Flow<List<FoodCard>> {
        return withContext(Dispatchers.IO) {
            try {
                foodCardDao.getAllFoodCards()
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error retrieving all FoodCards: ${e.message}")
                // Return an empty Flow in case of error
                emptyFlow()
            }
        }
    }

    /**
     * Get all foodCard entries from the database by related foodId.
     * @param foodId of related food.
     */
    suspend fun getFoodCardsByFoodId(foodId: String): Flow<List<FoodCard>> {
        return withContext(Dispatchers.IO) {
            try {
                foodCardDao.getFoodCardsByFoodId(foodId)
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error retrieving FoodCards by foodId: ${e.message}")
                emptyFlow()
            }
        }
    }

    /**
     * Get all foodCard entries from the database by related food category.
     * @param category of related food.
     */
    suspend fun getFoodCardByFoodCategory(category: String): Flow<List<FoodCard>> {
        return withContext(Dispatchers.IO) {
            try {
                foodCardDao.getFoodCardsByCategory(category)
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error retrieving FoodCards by category: ${e.message}")
                emptyFlow()
            }
        }
    }
}

package com.example.projektmbun.controller

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.projektmbun.exceptions.FoodCardCreationException
import com.example.projektmbun.exceptions.FoodCardUpdateException
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Controller class responsible for managing `FoodCard` operations, including
 * creation, deletion, updates, and queries. It interacts with both Room (FoodCard) and Supabase (Food).
 */
class FoodCardController(
    private val foodCardDao: FoodCardDao,
    private val foodService: FoodService
) {

    /**
     * Adds a new food card to the local database.
     * Ensures the associated food item exists in Supabase before adding the card.
     * @param foodCard The food card to add.
     * @throws FoodCardCreationException if the associated food item does not exist or if the creation fails.
     */
    suspend fun addFoodCard(foodCard: FoodCard) {
        withContext(Dispatchers.IO) {
            try {
                // Check if the associated food exists in Supabase
                val foodExists = foodService.getFoodByName(foodCard.foodId).isNotEmpty()
                if (!foodExists) {
                    throw FoodCardCreationException("Food item with ID '${foodCard.foodId}' does not exist in Supabase.")
                }

                // Insert the FoodCard locally
                val createdFoodCardId = foodCardDao.insertFoodCard(foodCard).toInt()
                if (createdFoodCardId <= 0) {
                    throw FoodCardCreationException("Failed to create FoodCard: Invalid Id returned.")
                }
            } catch (e: SQLiteConstraintException) {
                throw FoodCardCreationException("FoodCard creation failed due to a database constraint.")
            } catch (e: Exception) {
                Log.e("FoodCardController", "Unexpected error: ${e.localizedMessage}", e)
                throw e
            }
        }
    }

    /**
     * Deletes a FoodCard by its ID.
     * @param foodCardId The ID of the FoodCard to delete.
     * @throws IllegalArgumentException if the ID is invalid.
     * @throws Exception if the deletion fails.
     */
    suspend fun deleteFoodCardById(foodCardId: Int) = withContext(Dispatchers.IO) {
        if (foodCardId <= 0) {
            throw IllegalArgumentException("Invalid FoodCard ID: $foodCardId")
        }

        try {
            val rowsDeleted = foodCardDao.deleteFoodCardById(foodCardId)
            if (rowsDeleted == 0) {
                throw Exception("Failed to delete FoodCard with ID: $foodCardId - No rows removed.")
            }
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error deleting FoodCard with ID $foodCardId: ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Retrieves all food cards with their associated food details from Supabase.
     * @return A list of `FoodCardWithDetails` objects.
     */
    suspend fun getFoodCardsWithDetails(): List<FoodCardWithDetails> = withContext(Dispatchers.IO) {
        try {
            // Fetch all FoodCards from the local database
            val foodCards = foodCardDao.getAllFoodCards()

            // Fetch associated Food items from Supabase
            val foodIds = foodCards.map { it.foodId }
            val foods = foodService.getFoodsByNames(foodIds)

            // Combine FoodCards with their corresponding Food
            foodCards.mapNotNull { foodCard ->
                val food = foods.find { it.name == foodCard.foodId }
                if (food != null) FoodCardWithDetails(foodCard, food) else null
            }
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error fetching food cards with details: ${e.localizedMessage}")
            emptyList()
        }
    }

    /**
     * Updates the expiry date of a food card.
     * @param foodCardId The ID of the food card to update.
     * @param newExpiryDate The new expiry date.
     * @throws IllegalArgumentException if the expiry date is not valid.
     * @throws FoodCardUpdateException if the update fails.
     */
    suspend fun updateExpiryDateByFoodCardId(foodCardId: Int, newExpiryDate: String) {
        if (!isValidIsoDate(newExpiryDate)) {
            throw IllegalArgumentException("Invalid expiry date: $newExpiryDate")
        }

        withContext(Dispatchers.IO) {
            try {
                val rowsUpdated = foodCardDao.updateExpiryDateByFoodCardId(foodCardId, newExpiryDate)
                if (rowsUpdated == 0) {
                    throw FoodCardUpdateException("Failed to update FoodCard with ID: $foodCardId - No rows updated.")
                }
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error updating expiry date: ${e.localizedMessage}")
                throw e
            }
        }
    }

    /**
     * Retrieves all FoodCards associated with a given Routine ID and includes their Food details from Supabase.
     * @param routineId The ID of the routine.
     * @return A list of `FoodCardWithDetails` objects containing FoodCards and their associated Food details.
     */
    suspend fun getFoodCardsByRoutineId(routineId: Int): List<FoodCardWithDetails> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch FoodCards associated with the Routine ID from the local database
            val foodCards = foodCardDao.getFoodCardsByRoutineId(routineId)

            // 2. Extract food IDs from the FoodCards
            val foodIds = foodCards.map { it.foodId }

            // 3. Fetch Food details from Supabase using the extracted food IDs
            val foods = foodService.getFoodsByNames(foodIds)

            // 4. Combine FoodCards with their corresponding Food details
            foodCards.mapNotNull { foodCard ->
                val food = foods.find { it.name == foodCard.foodId }
                if (food != null) FoodCardWithDetails(foodCard, food) else null
            }
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error fetching FoodCards by Routine ID: ${e.localizedMessage}")
            emptyList()
        }
    }

    /**
     * Retrieves all FoodCards that are currently in stock, along with their Food details from Supabase.
     * @return A list of `FoodCardWithDetails` objects.
     */
    suspend fun getFoodCardsInStock(): List<FoodCardWithDetails> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch FoodCards in stock from the local database
            val foodCards = foodCardDao.getFoodCardsInStock()

            // 2. Extract food IDs from the FoodCards
            val foodIds = foodCards.map { it.foodId }

            // 3. Fetch Food details from Supabase using the extracted food IDs
            val foods = foodService.getFoodsByNames(foodIds)

            // 4. Combine FoodCards with their corresponding Food details
            foodCards.mapNotNull { foodCard ->
                val food = foods.find { it.name == foodCard.foodId }
                if (food != null) FoodCardWithDetails(foodCard, food) else null
            }
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error fetching FoodCards in stock: ${e.localizedMessage}")
            emptyList()
        }
    }

    /**
     * Retrieves all FoodCards by a search query for their name, along with their Food details from Supabase.
     * @param query The search query for the Food name.
     * @return A list of `FoodCardWithDetails` objects.
     */
    suspend fun getFoodCardsByName(query: String): List<FoodCardWithDetails> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch all FoodCards from the local database
            val foodCards = foodCardDao.getAllFoodCards()

            // 2. Fetch Food details matching the query from Supabase
            val foods = foodService.getFoodByName(query)

            // 3. Combine FoodCards with their corresponding Food details
            foodCards.mapNotNull { foodCard ->
                val food = foods.find { it.name == foodCard.foodId }
                if (food != null) FoodCardWithDetails(foodCard, food) else null
            }
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error fetching FoodCards by name: ${e.localizedMessage}")
            emptyList()
        }
    }

    /**
     * Removes a FoodCard from stock by setting its stockId to null.
     * @param foodCardId The ID of the FoodCard to remove from stock.
     * @throws IllegalArgumentException if the ID is invalid.
     * @throws Exception if the update fails.
     */
    suspend fun removeFoodCardFromStock(foodCardId: Int) = withContext(Dispatchers.IO) {
        if (foodCardId <= 0) {
            throw IllegalArgumentException("Invalid FoodCard ID: $foodCardId")
        }

        try {
            val rowsUpdated = foodCardDao.updateFoodCardStockIdByFoodCardId(foodCardId, null)
            if (rowsUpdated == 0) {
                throw Exception("Failed to remove FoodCard with ID: $foodCardId from stock - No rows updated.")
            }
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error removing FoodCard with ID $foodCardId from stock: ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Helper function to validate ISO date format.
     */
    private fun isValidIsoDate(date: String): Boolean {
        return try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            true
        } catch (e: Exception) {
            false
        }
    }
}
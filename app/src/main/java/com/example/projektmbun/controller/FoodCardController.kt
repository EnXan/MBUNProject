package com.example.projektmbun.controller

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.projektmbun.exceptions.FoodCardCreationException
import com.example.projektmbun.exceptions.FoodCardDeletionException
import com.example.projektmbun.exceptions.FoodCardNotFoundException
import com.example.projektmbun.exceptions.FoodCardUpdateException
import com.example.projektmbun.models.daos.FoodCardDao
import com.example.projektmbun.models.daos.FoodCardWithDetailsDao
import com.example.projektmbun.models.data.food_card.FoodCard
import com.example.projektmbun.utils.enums.FoodCardStateEnum
import com.example.projektmbun.models.data.relations.FoodCardWithDetails
import com.example.projektmbun.utils.SearchLogic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


/**
 * Controller class responsible for managing `FoodCard` operations, including
 * creation, deletion, updates, and queries. It interacts with DAO classes to
 * perform database operations and provides business logic for handling `FoodCard`-related functionality.
 *
 * @property foodCardDao DAO for basic `FoodCard` operations.
 * @property foodCardWithDetailsDao DAO for `FoodCard` operations with related details.
 */
class FoodCardController(
    private val foodCardDao: FoodCardDao,
    private val foodCardWithDetailsDao: FoodCardWithDetailsDao,
) {

    /**
     * Adds a new food card to the database.
     * @param foodCard the new food card to add.
     * @throws FoodCardCreationException when an invalid id returned or a database constraint exception occurs.
     * @throws Exception when an unexpected error occurs.
     */
    suspend fun addFoodCard(foodCard: FoodCard) {
        withContext(Dispatchers.IO) {
            try {
                val createdFoodCardId = foodCardDao.insertFoodCard(foodCard).toInt()

                //if food card could not be created
                if (createdFoodCardId <= 0) {
                    throw FoodCardCreationException("Failed to create FoodCard: Invalid Id returned: $createdFoodCardId")
                }

            }
            catch (e: SQLiteConstraintException) {
                throw FoodCardCreationException("FoodCard creation failed due to a database constraint.")
            }
            catch (e: Exception) {
                Log.e("FoodCardController", "Unexpected error: ${e.localizedMessage}", e)
                throw e
            }
        }
    }

    /**
     * Removes a existing food card to the database.
     * @param id the id of the food card to remove.
     * @throws IllegalArgumentException when the id is invalid.
     * @throws FoodCardDeletionException when no rows were affected or a database constraint exception occurs.
     * @throws Exception when an unexpected error occurs.
     */
    suspend fun deleteFoodCardById(id: Int) {
        if(id <= 0) {
            throw IllegalArgumentException("Invalid FoodCard Id: $id")
        }

        withContext(Dispatchers.IO) {
            try {
                val rowsDeleted = foodCardDao.deleteFoodCardById(id)

                if(rowsDeleted == 0) {
                    throw FoodCardDeletionException("Failed to delete FoodCard with id: $id - No rows removed")
                }
            }
            catch (e: SQLiteConstraintException) {
                throw FoodCardDeletionException("FoodCard deletion failed due to a database constraint.")
            }
            catch (e: Exception) {
                Log.e("FoodCardController", "Unexpected error: ${e.localizedMessage}", e)
                throw e
            }
        }
    }


    /**
     * Removes an existing food card from the stock and deletes it completely if it is no longer referenced
     * by either `stockId` or `routineId`.
     *
     * @param foodCardId the ID of the food card to be removed.
     * @throws IllegalArgumentException if the provided food card ID is invalid (e.g., less than or equal to 0).
     * @throws FoodCardNotFoundException if the food card could not be found in the database.
     * @throws FoodCardUpdateException if no rows were updated while attempting to remove the stock reference from the food card.
     * @throws FoodCardDeletionException if deletion failed due to a database constraint violation or no rows were affected during deletion.
     * @throws Exception for any other unexpected errors during the operation.
     */
    suspend fun removeFoodCardFromStock(foodCardId: Int) = withContext(Dispatchers.IO) {
        if (foodCardId <= 0) {
            throw IllegalArgumentException("Invalid FoodCard Id: $foodCardId")
        }

        try {
            val rowsUpdated = foodCardDao.updateFoodCardStockIdByFoodCardId(foodCardId, null)
            if (rowsUpdated == 0) {
                throw FoodCardUpdateException("Failed to update FoodCard with id: $foodCardId - No rows updated.")
            }

            try {
                deleteIfUnreferenced(foodCardId)
            } catch (e: FoodCardNotFoundException) {
                Log.e("FoodCardController", "FoodCard not found during deletion: ${e.localizedMessage}")
                throw e
            } catch (e: FoodCardDeletionException) {
                Log.e("FoodCardController", "Deletion failed: ${e.localizedMessage}")
                throw e
            }
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error removing FoodCard from stock: ${e.localizedMessage}", e)
            throw e
        }
    }


    /**
     * Get all food cards related to the routine Id.
     * @param routineId the id of the routine to get the food cards from.
     * @return List of food cards related to the routine Id or empty list `List<FoodCard>`.
     */
    suspend fun getFoodCardsByRoutineId(routineId: Int): List<FoodCard> {
        return withContext(Dispatchers.IO) {
            try {
                foodCardDao.getFoodCardsByRoutineId(routineId)
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error fetching food cards by routineId: ${e.message}")
                emptyList()
            }
        }
    }


    /**
     * Get all food cards by search query with related food information included.
     * @param name the search query.
     * @return List of food cards that match the search query or empty list `List<FoodCardWithDetails>`.
     */
    suspend fun getFoodCardsByName(name: String): List<FoodCardWithDetails> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        try {
            val allFoodCards = foodCardWithDetailsDao.getAllFoodCardsWithDetails()

            if (trimmedName.isBlank()) {
                return@withContext allFoodCards
            }

            //call Fuzzy-Search if name not empty
            SearchLogic.fuzzySearch(
                query = trimmedName,
                items = allFoodCards,
                nameSelector = { foodCard -> foodCard.food.name },
                threshold = -3
            )
        } catch (e: Exception) {
            Log.e("FoodCardController", "Error fetching food cards: ${e.message}")
            emptyList()
        }
    }

    /**
     * Updates the expiry date of a food card.
     * @param foodCardId the id of the food card to update the expiry date.
     * @param newExpiryDate the new expiry date for the food card.
     * @throws IllegalArgumentException if the expiry date is not right formatted.
     * @throws FoodCardUpdateException when no rows were updated.
     * @throws Exception when an unexpected error occurs.
     */
    suspend fun updateExpiryDateByFoodCardId(foodCardId: Int, newExpiryDate: String) {

        if(!isValidIsoDate(newExpiryDate)) {
            throw IllegalArgumentException("Invalid expiry date: $newExpiryDate")
        }

        return withContext(Dispatchers.IO) {
            try {
                val rowsUpdated =foodCardDao.updateExpiryDateByFoodCardId(foodCardId, newExpiryDate)
                if(rowsUpdated == 0) {
                    throw FoodCardUpdateException("Failed to update FoodCard with id: $foodCardId - No rows updated")
                }
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error updating expiry date: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Helper function to check if the given string is a valid ISO date.
     * @return true if right formatted. False if not right formatted.
     */
    private fun isValidIsoDate(date: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            LocalDate.parse(date, formatter)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    /**
     * Get the food card by its state with related food information.
     * @param state of the food card.
     * @return List of food cards with the given state or empty list `List<FoodCardWithDetails>`.
     */
    suspend fun getFoodCardsByState(state: FoodCardStateEnum): List<FoodCardWithDetails> {
            return withContext(Dispatchers.IO) {
                try {
                    foodCardWithDetailsDao.getFoodCardWithDetailsByState(state)
                } catch (e: Exception) {
                    Log.e("FoodCardController", "Error fetching food cards by state: ${e.message}")
                    emptyList()
                }
            }
        }

    suspend fun getFoodCardsInStock(): List<FoodCardWithDetails> {
        return withContext(Dispatchers.IO) {
            try {
                foodCardWithDetailsDao.getFoodCardWithDetailsInStock()
            } catch (e: Exception) {
                Log.e("FoodCardController", "Error fetching food cards in stock: ${e.message}")
                emptyList()
            }
        }
    }

    /**
     * Deletes a food card if it has no references to stockId or routineId.
     * @param foodCardId the id of the food card to check.
     * @throws FoodCardNotFoundException if food card not found.
     * @throws FoodCardDeletionException if no rows were affected or deletion failed tue to a database constraint.
     * @throws Exception if an unexpected error occurs.
     */
    private suspend fun deleteIfUnreferenced(foodCardId: Int) = withContext(Dispatchers.IO) {
        val foodCard = foodCardDao.getFoodCardById(foodCardId)
            ?: throw FoodCardNotFoundException("FoodCard with id $foodCardId could not be found.")

        if (foodCard.routineId == null && foodCard.stockId == null) {
            try {
                val rowsDeleted = foodCardDao.deleteFoodCardById(foodCardId)
                if (rowsDeleted == 0) {
                    throw FoodCardDeletionException("Failed to delete FoodCard with id: $foodCardId - No rows removed.")
                }
            } catch (e: SQLiteConstraintException) {
                throw FoodCardDeletionException("FoodCard deletion failed due to a database constraint: ${e.localizedMessage}")
            } catch (e: Exception) {
                Log.e("FoodCardController", "Unexpected error during FoodCard deletion: ${e.localizedMessage}", e)
                throw FoodCardDeletionException("Unexpected error while deleting FoodCard: ${e.localizedMessage}")
            }
        }
    }

}

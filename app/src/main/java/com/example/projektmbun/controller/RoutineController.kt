package com.example.projektmbun.controller

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.util.Log
import com.example.projektmbun.exceptions.RoutineDeletionException
import com.example.projektmbun.exceptions.RoutineNotUpdatedException
import com.example.projektmbun.models.daos.FoodCardDao
import com.example.projektmbun.models.daos.RoutineDao
import com.example.projektmbun.models.data.food_card.FoodCard
import com.example.projektmbun.utils.enums.FoodCardStateEnum
import com.example.projektmbun.models.data.routine.Routine
import com.example.projektmbun.views.fragments.StockFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


/**
 * Controller class responsible for managing `Routine` operations, including
 * CRUD operations for routines, adding/removing food cards to/from routines,
 * and executing routines based on schedules.
 *
 * @property routineDao DAO for performing `Routine` operations.
 * @property foodCardDao DAO for performing `FoodCard` operations.
 */
class RoutineController(
    private val routineDao: RoutineDao,
    private val foodCardDao: FoodCardDao,
) {

    /**
     * Get all routines.
     * @return Mutable list of all routines or empty mutable list `MutableList<Routine>`.
     * @throws Exception if an error occurs while fetching the routines.
     */
    suspend fun getAllRoutines(): MutableList<Routine> = withContext(Dispatchers.IO) {
        try {
            routineDao.getAllRoutines().toMutableList()
        } catch (e: Exception) {
            Log.e("RoutineController", "Error fetching all routines: ${e.localizedMessage}", e)
            mutableListOf()
        }
    }

    /**
     * Update the isActive status of a routine.
     * @param id of the routine.
     * @param isActive the new status.
     * @throws RoutineNotUpdatedException if no rows were updated.
     * @throws Exception if an error occurs while updating the isActive status.
     */
    suspend fun updateRoutineIsActive(id: Int, isActive: Boolean) = withContext(Dispatchers.IO) {
        try {
            val rowsUpdated = routineDao.updateIsActive(id, isActive)

            if(rowsUpdated == 0) {
                throw RoutineNotUpdatedException("No rows were updated")
            }
        } catch (e: Exception) {
            Log.e("RoutineController", "Error updating isActive status: ${e.localizedMessage}", e)
            throw e
        }
    }

    /**
     * Get all active routines.
     * @return List of all active routines or empty list `List<Routine>`.
     * @throws Exception if an error occurs while fetching the active routines.
     */
    suspend fun getAllActiveRoutines(): List<Routine> = withContext(Dispatchers.IO) {
        try {
            routineDao.getAllActiveRoutines()
        } catch (e: Exception) {
            Log.e("RoutineController", "Error fetching all active routines: ${e.localizedMessage}", e)
            emptyList()
        }
    }

    /**
     * Add or update a routine.
     * @param routine the routine to add or update.
     * @throws Exception if an error occurs while adding or updating the routine.
     */
    suspend fun addOrUpdateRoutine(routine: Routine) = withContext(Dispatchers.IO) {
        try {
            routineDao.upsertRoutine(routine)
        } catch (e: Exception) {
            Log.e("RoutineController", "Error adding/updating Routine with id: ${routine.id}. Exception: ${e.localizedMessage}", e)
            throw e
        }
    }

    /**
     * Routine löschen.
     * @param routineId the id of the routine to delete.
     * @throws RoutineDeletionException if an error occurs while deleting the routine.
     * @throws Exception if an unexpected error occurs.
     */
    suspend fun deleteRoutine(routineId: Int) = withContext(Dispatchers.IO) {
        try {
            val deletedRows = routineDao.deleteRoutineById(routineId)
            if (deletedRows == 0) {
                throw RoutineDeletionException("Error deleting Routine with id: $routineId. No rows were deleted.")
            }
        } catch (e: Exception) {
            Log.e("RoutineController", "Error deleting Routine with id: $routineId. Exception: ${e.localizedMessage}", e)
            throw e
        }
    }

    /**
     * Add a new food card to a routine.
     * @param foodCard the food card to add.
     * @param routineId the id of the routine to add the food card to.
     * @throws Exception if an error occurs while adding the food card to the routine.
     */
    suspend fun addFoodCardToRoutine(foodCard: FoodCard, routineId: Int?) = withContext(Dispatchers.IO) {

        //Change food card properties to match the routine requirements
        val updatedFoodCard = foodCard.copy(isActive = true, routineId = routineId, state = FoodCardStateEnum.TEMPORARY)

            try {
                foodCardDao.insertFoodCard(updatedFoodCard)
            }
            catch (e: Exception) {
                Log.e("Routine Controller", "Error adding FoodCard to Routine with id: $routineId. Exception: ${e.localizedMessage}", e)
                throw e
        }
    }


    /**
     * Remove a food card from a routine.
     * @param foodCard the food card to remove.
     * @throws NullPointerException if the food card id is null.
     * @throws Exception if an error occurs while removing the food card to the routine.
     */
    suspend fun removeFoodCardFromRoutine(foodCard: FoodCard) = withContext(Dispatchers.IO) {
        requireNotNull(foodCard.id) { "FoodCard id must not be null" }

        try {
            // remove reference to routine
            val rowsUpdated = foodCardDao.updateFoodCardRoutineIdByFoodCardId(foodCard.id, null)
            if (rowsUpdated == 0) {
                Log.e("RoutineController", "No rows updated for FoodCard with id: ${foodCard.id}")
                return@withContext
            }

            // get updated food card from database
            val updatedFoodCard = foodCardDao.getFoodCardById(foodCard.id)
            if (updatedFoodCard == null) {
                Log.e("RoutineController", "Failed to fetch updated FoodCard with id: ${foodCard.id}")
                return@withContext
            }

            // check, if food card can be deleted
            if (checkForDeletion(updatedFoodCard)) {
                foodCardDao.deleteFoodCardById(updatedFoodCard.id!!)
            } else {
                Log.d("RoutineController", "Cannot delete FoodCard with id: ${updatedFoodCard.id}")
            }
        } catch (e: Exception) {
            Log.e("RoutineController", "Error while removing FoodCard from routine: ${e.localizedMessage}", e)
        }
    }


    /**
     * Helper function to check if food card can be deleted.
     * @param foodCard the food card to check for.
     * @return `true` if no reference is set `false` if a reference is set.
     */
    private fun checkForDeletion(foodCard: FoodCard): Boolean {
        return foodCard.routineId == null && foodCard.stockId == null
    }


    private suspend fun changeState(foodCard: FoodCard) {
        if(foodCard.id != null) {
            if (foodCard.stockId != null) {
                foodCardDao.updateStateByFoodCardId(foodCard.id, FoodCardStateEnum.PERMANENT)
            }
            foodCardDao.updateStateByFoodCardId(foodCard.id, FoodCardStateEnum.TEMPORARY)
        }
    }

    suspend fun checkAndExecuteRoutines() = withContext(Dispatchers.IO) {
        val activeRoutines = getAllActiveRoutines()

        activeRoutines.forEach { routine ->
            if (isRoutineDue(routine.startDate, routine.rhythm)) {
                transferFoodCardsToStock(routine)
                Log.d("RoutineController", "Routine ${routine.name} executed")
            } else {
                Log.d("RoutineController", "Routine ${routine.name} is not due yet")
            }
        }
    }

    private fun isRoutineDue(routineStartDate: String, interval: String): Boolean {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) // Passe das Format an
        val startDate = formatter.parse(routineStartDate)
        val today = Calendar.getInstance().time

        val diff = ((today.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()

        return when (interval) {
            "Täglich" -> true
            "Wöchentlich" -> diff % 7 == 0
            "Alle 2 Wochen" -> diff % 14 == 0
            "Alle 3 Wochen" -> diff % 21 == 0
            "Alle 4 Wochen" -> diff % 28 == 0
            else -> false
        }
    }

    suspend fun executeRoutineIfDueToday(routine: Routine) {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) // Passe das Format an
        val today = formatter.format(Calendar.getInstance().time)

        if (routine.startDate == today || isRoutineDue(routine.startDate, routine.rhythm)) {
            transferFoodCardsToStock(routine)
            Log.d("RoutineController", "Routine ${routine.name} wurde heute ausgeführt")
        } else {
            Log.d("RoutineController", "Routine ${routine.name} ist nicht fällig")
        }
    }


    private suspend fun transferFoodCardsToStock(routine: Routine) {
        val foodCards = foodCardDao.getFoodCardsByRoutineId(routine.id!!)
        foodCards.forEach { foodCard ->
            foodCardDao.updateStateByFoodCardId(foodCard.id!!, FoodCardStateEnum.BOTH) // Setze Status auf both
            foodCardDao.updateFoodCardStockIdByFoodCardId(foodCard.id, stockId = 1)
            Log.d("RoutineController", "Transferred FoodCard ${foodCard.id} from Routine ${routine.name} to Stock")
        }
    }
}


package com.example.projektmbun.models.local.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.projektmbun.models.data_structure.relations.RoutineWithFoodCards

/**
 * Data Access Object (DAO) interface for performing database operations on the `Routine` entity
 * along with its related food cards. Provides methods for retrieving routines with their associated
 * food cards.
 */
@Dao
interface RoutineWithFoodCardsDao {

    /**
     * Get a routine with its food cards.
     * @param routineId the id of the routine to get.
     * @return a routine with its food cards `RoutineWithFoodCards` or null if not found `null`.
     */
    @Transaction
    @Query("""
        SELECT * 
        FROM Routine
        WHERE id = :routineId
    """)
    suspend fun getRoutineWithFoodCards(routineId: Int): RoutineWithFoodCards?


    /**
     * Get all routines with their food cards.
     * @return a list of routines with their food cards or empty list if no found `List<RoutineWithFoodCards>`.
     */
    @Transaction
    @Query("""
        SELECT * 
        FROM Routine
    """)
    suspend fun getAllRoutinesWithFoodCards(): List<RoutineWithFoodCards>
}
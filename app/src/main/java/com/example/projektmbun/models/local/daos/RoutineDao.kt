package com.example.projektmbun.models.local.daos

import androidx.room.*
import com.example.projektmbun.models.data_structure.routine.Routine
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for performing database operations on the `Routine` entity.
 * Provides methods for retrieving, inserting, updating, and deleting routines.
 */
@Dao
interface RoutineDao {

    /**
     * Get all routines.
     * @return a list of all routines or empty list if no routines are found `List<Routine>`.
     */
    @Query("SELECT * FROM routine")
    suspend fun getAllRoutines(): List<Routine>

    /**
     * Get routine by id.
     * @param id the id of the routine to get.
     * @return a routine with the given id `Routine` or null if not found `null`.
     */
    @Query("SELECT * FROM routine WHERE id = :id")
    suspend fun getRoutineById(id: Int): Routine?

    /**
     * Get routines by most matching name.
     * @param name the name of the recipes.
     * @return a list of all matching routines or empty list if no routines are found `List<Routine>`.
     */
    @Query("SELECT * FROM routine WHERE name LIKE '%' || :name || '%'")
    suspend fun getRoutinesByName(name: String): List<Routine>

    /**
     * Get all active routines.
     * @return a list of all active routines or empty list if no routines are found `List<Routine>`.
     */
    @Query("SELECT * FROM routine WHERE isActive = 1")
    suspend fun getAllActiveRoutines(): List<Routine>

    /**
     * Insert a new routine in the database or update an existing routine.
     * @param routine a new routine to upsert.
     * @return the id of the created or updated routine `Long`.
     */
    @Upsert
    suspend fun upsertRoutine(routine: Routine): Long


    /**
     * Update the isActive property of a routine.
     * @param routineId the id of the routine to update.
     * @param isActive the new value for the isActive property.
     * @return number of rows updated `Int`.
     */
    @Query("UPDATE routine SET isActive = :isActive WHERE id = :routineId")
    suspend fun updateIsActive(routineId: Int, isActive: Boolean) : Int

    /**
     * Delete a existing routine by it's id.
     * @param routineId the id of the routine to delete.
     * @return the affected rows in the database `Int`.
     */
    @Query("DELETE FROM routine WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: Int): Int

    @Query("SELECT COUNT(*) FROM routine")
    fun getRoutineCountFlow(): Flow<Int>

}

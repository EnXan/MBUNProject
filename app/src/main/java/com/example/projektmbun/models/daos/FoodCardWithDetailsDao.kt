package com.example.projektmbun.models.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.projektmbun.utils.enums.FoodCardStateEnum
import com.example.projektmbun.models.data.relations.FoodCardWithDetails

/**
 * Data Access Object (DAO) interface for performing database operations on the `FoodCard` entity
 * and its related details. Provides methods for retrieving `FoodCardWithDetails` objects that
 * include additional relational data.
 */
@Dao
interface FoodCardWithDetailsDao {

    /**
     * Get food card with details by food card id.
     * @param foodCardId the id of the food card to get.
     * @return a FoodCardWithDetails containing the food card and its details `FoodCardWithDetails` or null if not found `null`.
     */
    @Transaction
    @Query("""
        SELECT *
        FROM food_card
        WHERE id = :foodCardId
    """)
    suspend fun getFoodCardWithDetails(foodCardId: Int): FoodCardWithDetails?

    @Transaction
    @Query("""
        SELECT *
        FROM food_card
        WHERE state = "PERMANENT" OR state = "BOTH"
    """)
    suspend fun getFoodCardWithDetailsInStock(): List<FoodCardWithDetails>

    /**
     * Get all food cards with details.
     * @return a list of FoodCardWithDetails containing all food cards and their details `FoodCardWithDetails`.
     */
    @Transaction
    @Query("SELECT * FROM food_card")
    fun getAllFoodCardsWithDetails(): List<FoodCardWithDetails>

    /**
     * Get all food cards with a specific state. State is either <b>temporary</b> or <b>permanent</b>
     * @param state the state of the foodCard to get.
     * @return a list of all food cards with the given state or empty list if no found `List<FoodCardWithDetails>`.
     */
    @Transaction
    @Query("SELECT * FROM food_card WHERE state = :state")
    suspend fun getFoodCardWithDetailsByState(state: FoodCardStateEnum): List<FoodCardWithDetails>

    /**
     * Get all food cards with a specific routine id. State is either <b>temporary</b> or <b>permanent</b>
     * @param routineId the routine id of the foodCard to get.
     * @return a list of all food cards with the given routine id or empty list if no found `List<FoodCardWithDetails>`.
     */
    @Transaction
    @Query("SELECT * FROM food_card WHERE routineId = :routineId")
    suspend fun getFoodCardWithDetailsByRoutineId(routineId: Int): List<FoodCardWithDetails>


}
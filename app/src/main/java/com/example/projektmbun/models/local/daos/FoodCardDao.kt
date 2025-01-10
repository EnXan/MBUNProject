package com.example.projektmbun.models.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import kotlinx.coroutines.flow.Flow


/**
 * Data Access Object (DAO) interface for performing database operations on the `FoodCard` entity.
 * Provides methods for inserting, updating, deleting, and retrieving food cards, as well as specific queries
 * based on relationships such as `routineId` or `stockId`.
 */
@Dao
interface FoodCardDao {

    /**
     * Insert a food card into the database.
     * @param foodCard the food card to insert.
     * @return id of the inserted food card `Long`.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodCard(foodCard: FoodCard) : Long

    /**
     * Update a food card in the database.
     * @param foodCard the food card to update.
     * @return number of rows updated `Int`.
     */
    @Update
    suspend fun updateFoodCard(foodCard: FoodCard) : Int

    /**
     * Delete a food card from the database by its id.
     * @param foodCardId the id of the food card to delete.
     * @return number of rows deleted `Int`.
     */
    @Query("DELETE FROM food_card WHERE id = :foodCardId")
    suspend fun deleteFoodCardById(foodCardId: Int) : Int

    /**
     * Get all food cards from the database.
     * @return list of all food cards `List<FoodCard>`.
     */
    @Query("SELECT * FROM food_card")
    suspend fun getAllFoodCards() : List<FoodCard>

    /**
     * Get a food card by its id.
     * @param foodCardId the id of the food card to get.
     * @return food card with the given id `FoodCard` or null if not found `null`.
     */
    @Query("SELECT * FROM food_card WHERE id = :foodCardId")
    suspend fun getFoodCardById(foodCardId: Int) : FoodCard?

    @Query("SELECT * FROM food_card WHERE id = :foodCardId AND stockId = :stockId")
    suspend fun getFoodCardByIdAndStockId(foodCardId: Int?, stockId: Int?) : FoodCard?
    /**
     * Get food card by their routine id.
     * @param routineId the id of the routines to get.
     * @return food card with the given routines id or empty list if no related routine id found `List<FoodCard>`.
     */
    @Query("SELECT * FROM food_card WHERE routineId = :routineId")
    suspend fun getFoodCardsByRoutineId(routineId: Int): List<FoodCard>

    /**
     * Get a food card by its stock id.
     * @param stockId the id of the stock to get.
     * @return food card with the given stock id `FoodCard` or null if no related stock id found `null`.
     */
    @Query("SELECT * FROM food_card WHERE id = :stockId")
    suspend fun getFoodCardByStockId(stockId: Int) : FoodCard?

    @Query("SELECT COUNT(*) FROM food_card WHERE stockId IS NOT NULL")
    fun getFoodCardCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM routine")
    fun getRoutineCountFlow(): Flow<Int>

    /**
     * Update the expiryDate of a food card by its id.
     * @param foodCardId the id of the food card to update.
     * @param expiryDate the new expiry date.
     * @return number of rows updated `Int`.
     */
    @Query("UPDATE food_card SET expiryDate = :expiryDate WHERE id = :foodCardId")
    suspend fun updateExpiryDateByFoodCardId(foodCardId: Int, expiryDate: String) : Int


    /**
     * Update the routineId of a food card by its id.
     * @param foodCardId the id of the food card to update.
     * @param routineId the new routine id.
     * @return number of rows updated `Int`.
     */
    @Query("UPDATE food_card SET routineId = :routineId WHERE id = :foodCardId")
    suspend fun updateFoodCardRoutineIdByFoodCardId(foodCardId: Int, routineId: Int?) : Int

    /**
     * Update the stockId of a food card by its id.
     * @param foodCardId the id of the food card to update.
     * @param stockId the new stock id.
     * @return number of rows updated `Int`.
     */
    @Query("UPDATE food_card SET stockId = :stockId WHERE id = :foodCardId")
    suspend fun updateFoodCardStockIdByFoodCardId(foodCardId: Int, stockId: Int?) : Int

    @Query("SELECT * FROM food_card WHERE stockId IS NOT NULL")
    suspend fun getFoodCardsInStock(): List<FoodCard>


}
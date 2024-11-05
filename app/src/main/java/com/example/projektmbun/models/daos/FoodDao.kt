package com.example.projektmbun.models.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.projektmbun.models.data.Food
import kotlinx.coroutines.flow.Flow

/**
 * Data access object to interact with food table of foodDatabase
 */
@Dao
interface FoodDao {

    /**
     * Add a new food or replace an existing one.
     * @param food The food to create or replace.
     */
    @Upsert
    suspend fun createOrReplaceFood(food: Food)

    /**
     * Delete a specific food.
     * @param food The food to be deleted.
     */
    @Delete
    suspend fun deleteFood(food: Food)


    /**
     * Get all foods by name.
     * @param name The name of the food to get.
     * @return Flow with a matching list of foods by name.
     */
    @Query("SELECT * FROM food WHERE food LIKE '%' || :name || '%'")
    fun getFoodsByName(name: String): Flow<List<Food>>

    /**
     * Get all foods by category.
     * @param category The category of the food to get.
     * @return Flow with a matching list of foods by category.
     */
    @Query("SELECT * FROM food WHERE category = :category")
    fun getFoodsByCategory(category: String): Flow<List<Food>>

    /**
     * Get all foods.
     * @return All foods.
     */
    @Query("SELECT * FROM food")
    fun getAllFood(): Flow<List<Food>>

    /**
     * Get all foods depending on search and category.
     * @return Flow with a matching list of foods
     */
    @Query("""
        SELECT * FROM food
        WHERE (:category IS NULL OR category = :category)
        AND (:query IS NULL OR food LIKE '%' || :query || '%')
    """)
    fun getFoodsByCategoryAndQuery(category: String?, query: String?): Flow<List<Food>>


}
package com.example.projektmbun.models.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.projektmbun.models.data.Food
import com.example.projektmbun.models.data.FoodCard
import kotlinx.coroutines.flow.Flow

/**
 * Data access object to interact with foodCard table of foodDatabase
 */
@Dao
interface FoodCardDao {

    /**
     * Add a new foodCard or replace an existing one.
     * @param foodCard The foodCard to create or replace.
     */
    @Upsert()
    suspend fun insertFoodCard(foodCard: FoodCard)

    /**
     * Get all foodCards by foodId
     * @param foodId The id of the food related to the foodCard.
     * @return a Flow of all matching foodCards by foodId.
     */
    @Query("SELECT * FROM food_card WHERE foodId = :foodId")
    fun getFoodCardsByFoodId(foodId: String): Flow<List<FoodCard>>


    /**
     * Delete a specific foodCard by id.
     * @param id The id of the foodCard.
     */
    @Query("DELETE FROM food_card WHERE id = :id")
    suspend fun deleteFoodCardById(id: Int)

    /**
     * Get all foodCards
     * @return a Flow of all foodCards
     */
    @Query("SELECT * FROM food_card")
    fun getAllFoodCards(): Flow<List<FoodCard>>

    /**
     * Get all foodCards by food category
     * @param category The category of the related food to the foodCard.
     * @return a Flow of all matching foodCards by food category.
     */
    @Query("""
    SELECT food_card.* FROM food_card
    INNER JOIN food ON food_card.foodId = food.food
    WHERE food.category = :category
""")
    fun getFoodCardsByCategory(category: String): Flow<List<FoodCard>>
}
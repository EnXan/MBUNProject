package com.example.projektmbun.models.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.utils.enums.FoodCategoryEnum

/**
 * Data Access Object (DAO) interface for performing database operations on the `Food` entity.
 * Provides methods for creating, retrieving, and filtering food records by name, category, or all records.
 */
@Dao
interface FoodDao {

    /**
     * Add a new food to the database.
     * @param food The food to create or replace.
     * @return Id of the created food `Long`.
     */
    @Insert
    suspend fun createFood(food: Food) : Long

    /**
     * Get all foods by name.
     * @param name The name of the food to get.
     * @return List of foods by name or empty list `List<Food>`.
     */
    @Query("SELECT * FROM food WHERE name LIKE '%' || :name || '%'")
    fun getFoodByName(name: String): List<Food>

    /**
     * Get all foods by category.
     * @param category The category of the food to get.
     * @return List of foods by category or empty list `List<Food>`.
     */
    @Query("SELECT * FROM food WHERE category = :category")
    fun getFoodByCategory(category: FoodCategoryEnum): List<Food>

    /**
     * Get all foods.
     * @return All foods or empty list if not found `List<Food>`.
     */
    @Query("SELECT * FROM food")
    fun getAllFood(): List<Food>

}
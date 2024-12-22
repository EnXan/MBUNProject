package com.example.projektmbun.models.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodLocal) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFood(food: FoodLocal)

    @Query("DELETE FROM food WHERE name = :foodName")
    suspend fun deleteFoodByName(foodName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFood(foods: List<FoodLocal>)

    @Query("SELECT * FROM food")
    fun getAllFood(): Flow<List<FoodLocal>>

    @Query("SELECT * FROM food WHERE name LIKE '%' || :name || '%'")
    fun getFoodByName(name: String): List<FoodLocal>

    @Query("SELECT * FROM food WHERE category = :category")
    suspend fun getFoodByCategory(category: FoodCategoryEnum): List<FoodLocal>

}
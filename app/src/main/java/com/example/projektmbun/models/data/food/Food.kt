package com.example.projektmbun.models.data.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.FoodStateEnum

/**
 * Represents a food item in the database.
 * Each food item is categorized and has an associated state.
 *
 * @property name The unique name of the food item (primary key).
 * @property category The category of the food item, defined in `FoodCategoryEnum` (e.g., vegetable, fruit, dairy).
 * @property state The state of the food item, defined in `FoodStateEnum` (e.g., fresh, frozen, canned).
 */
@Entity(tableName = "food")
data class Food(
    @PrimaryKey
    val name: String,
    val category: FoodCategoryEnum,
    val state: FoodStateEnum
)
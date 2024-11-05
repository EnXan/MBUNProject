package com.example.projektmbun.models.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * food table with a String of a food and a related category of type FoodCategoryEnum
 */
@Entity(tableName = "food")
data class Food(
    @PrimaryKey
    val food: String,
    val category: FoodCategoryEnum,
)
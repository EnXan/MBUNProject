package com.example.projektmbun.models.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A table of foodCards which have a integer primary key,
 * a foodId related to the food table,
 * a quantity
 * and a shelf life
 * These foodCards should be stored in the food stock
 */
@Entity(
    tableName = "food_card",
    indices = [Index(value = ["foodId"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Food::class,
        parentColumns = ["food"],
        childColumns = ["foodId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FoodCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodId: String,          // Refers to food in food table
    val quantity: Int,
    val expiringDate: LocalDate
)
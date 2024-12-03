package com.example.projektmbun.models.data.food_card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.models.data.routine.Routine
import com.example.projektmbun.models.data.stock.Stock
import com.example.projektmbun.utils.enums.FoodCardStateEnum
import com.example.projektmbun.utils.enums.UnitsEnum

/**
 * Represents a food card in the database, which links a specific food item with additional attributes
 * such as quantity, unit, expiry date, state, and associations with routines and stocks.
 *
 * @property id The unique identifier of the food card (auto-generated primary key).
 * @property foodId The name of the associated food item in the `food` table (foreign key).
 * @property quantity The quantity of the food item.
 * @property unit The unit of measurement for the quantity (e.g., grams, liters).
 * @property expiryDate The expiry date of the food item in ISO format (optional).
 * @property state The state of the food card (e.g., temporary, permanent).
 * @property isActive Indicates whether the food card is active.
 * @property routineId The ID of the associated routine in the `routine` table (foreign key, optional).
 *                     Deleting a routine sets this field to `null`.
 * @property stockId The ID of the associated stock in the `stock` table (foreign key, optional).
 *                   Deleting a stock sets this field to `null`.
 */
@Entity(
    tableName = "food_card",
    foreignKeys = [
        ForeignKey(
        entity = Food::class,
        parentColumns = ["name"],
        childColumns = ["foodId"],
        onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
                  ),
        ForeignKey(
            entity = Stock::class,
            parentColumns = ["id"],
            childColumns = ["stockId"],
            onDelete = ForeignKey.SET_NULL
        )],
)
data class FoodCard(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val foodId: String,          // Refers to food in food table
    var quantity: Double,
    var unit: UnitsEnum,
    var expiryDate: String?,
    var state: FoodCardStateEnum?,
    var isActive: Boolean,
    var routineId: Int? = null,
    var stockId: Int? = null
)
package com.example.projektmbun.models.data.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.utils.enums.UnitsEnum

/**
 * Represents an ingredient associated with a specific recipe in the database.
 * Each ingredient is linked to both a recipe and a food item, with cascading delete behavior.
 *
 * @property id The unique identifier of the ingredient (auto-generated primary key).
 * @property recipeId The ID of the associated recipe in the `recipes` table (foreign key).
 * @property name The name of the associated food item in the `food` table (foreign key).
 * @property description A description of the ingredient, providing additional details (optional).
 * @property amount The quantity of the ingredient required for the recipe (optional).
 * @property unit The unit of measurement for the ingredient's quantity (e.g., grams, liters), defined in `UnitsEnum`.
 * @property price The price of the ingredient.
 * @property isOptional Indicates whether the ingredient is optional for the recipe.
 */
@Entity(
    tableName = "ingredients",
    indices = [Index(value = ["id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Food::class,
            parentColumns = ["name"],
            childColumns = ["name"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val recipeId: Int,
    val name: String,
    val description: String?,
    val amount: Double?,
    val unit: UnitsEnum,
    val price: Double,
    val isOptional: Boolean
)
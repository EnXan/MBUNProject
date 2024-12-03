package com.example.projektmbun.models.data.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a dish type associated with a specific recipe in the database.
 * Each dish type belongs to a recipe, and deleting a recipe will cascade delete its associated dish types.
 *
 * @property id The unique identifier of the dish type (auto-generated primary key).
 * @property recipeId The ID of the associated recipe (foreign key).
 * @property type The name or category of the dish type (e.g., "Main Course", "Dessert").
 */
@Entity (
    tableName = "dish_types",
    indices = [Index(value = ["id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DishTypes(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val recipeId: Int,
    val type: String
)
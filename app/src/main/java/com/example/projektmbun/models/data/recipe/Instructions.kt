package com.example.projektmbun.models.data.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an instruction step associated with a specific recipe in the database.
 * Each instruction belongs to a recipe, and deleting the recipe will cascade delete its associated instructions.
 *
 * @property id The unique identifier of the instruction (auto-generated primary key).
 * @property recipeId The ID of the associated recipe in the `recipes` table (foreign key).
 * @property step The step number in the recipe's instructions (e.g., 1, 2, 3).
 * @property description The textual description of the instruction (e.g., "Preheat the oven to 180°C").
 * @property image A URL or path to an image representing the instruction step (optional).
 */
@Entity(
    tableName = "instructions",
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
data class Instructions(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val recipeId: Int,
    val step: Int,
    val description: String,
    val image: String
)
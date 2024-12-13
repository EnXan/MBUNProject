package com.example.projektmbun.models.data_structure.recipe

import kotlinx.serialization.Serializable

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
@Serializable
data class Instructions(
    val id: Int? = null,
    val recipeId: Int? = null,
    val step: Int,
    val description: String,
    val imageUrl: String?,
)
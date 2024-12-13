package com.example.projektmbun.models.data_structure.recipe

import kotlinx.serialization.Serializable

/**
 * Represents a piece of equipment associated with a specific instruction in a recipe.
 * Each equipment entry belongs to an instruction, and deleting the instruction will cascade delete
 * its associated equipment.
 *
 * @property id The unique identifier of the equipment entry (auto-generated primary key).
 * @property instructionId The ID of the associated instruction in the `instructions` table (foreign key).
 * @property equipment The name or description of the equipment (e.g., "Mixing Bowl", "Whisk").
 */
@Serializable
data class Equipment(
    val id: Int? = null,
    val instructionId: Int? = null,
    val equipment: String
)
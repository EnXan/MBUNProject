package com.example.projektmbun.models.data.recipe

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a detailed instruction that includes its associated equipment, ingredients, and notes.
 * Combines information from the `Instructions` entity and its related entities.
 *
 * @property instruction The instruction details from the `Instructions` entity.
 * @property equipment A list of `Equipment` objects associated with the instruction, linked by the `instructionId` property.
 */
data class InstructionWithEquipmentAndIngredientsAndNotes(
    @Embedded val instruction: Instructions,

    @Relation(
        parentColumn = "id",
        entityColumn = "instructionId"
    )
    val equipment: List<Equipment>,
)

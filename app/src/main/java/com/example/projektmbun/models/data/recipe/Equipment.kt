package com.example.projektmbun.models.data.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a piece of equipment associated with a specific instruction in a recipe.
 * Each equipment entry belongs to an instruction, and deleting the instruction will cascade delete
 * its associated equipment.
 *
 * @property id The unique identifier of the equipment entry (auto-generated primary key).
 * @property instructionId The ID of the associated instruction in the `instructions` table (foreign key).
 * @property equipment The name or description of the equipment (e.g., "Mixing Bowl", "Whisk").
 */
@Entity (
    tableName = "equipment",
    indices = [Index(value = ["id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = Instructions::class,
            parentColumns = ["id"],
            childColumns = ["instructionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Equipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val instructionId: Int,
    val equipment: String
)
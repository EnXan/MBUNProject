package com.example.projektmbun.models.data.routine

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a routine in the database, which schedules and manages recurring tasks or actions.
 *
 * @property id The unique identifier of the routine (auto-generated primary key).
 * @property name The name of the routine.
 * @property startDate The starting date of the routine in ISO format (e.g., "YYYY-MM-DD").
 * @property rhythm The recurrence interval of the routine (e.g., "Daily", "Weekly").
 * @property isActive Indicates whether the routine is currently active.
 */
@Entity(
    tableName = "routine"
)
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val startDate: String,
    val rhythm: String,
    var isActive: Boolean,
)

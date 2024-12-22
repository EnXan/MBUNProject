package com.example.projektmbun.models.data_structure.food

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import kotlinx.serialization.Serializable

/**
 * Represents a food item in the database.
 * Each food item is categorized and has an associated state.
 *
 * @property name The unique name of the food item (primary key).
 * @property category The category of the food item, defined in `FoodCategoryEnum` (e.g., vegetable, fruit, dairy).
 * @property state The state of the food item, defined in `FoodStateEnum` (e.g., fresh, frozen, canned).
 */
@Serializable // Für Supabase und JSON-Serialisierung
@Entity(tableName = "food") // Für Room-Datenbank
data class FoodLocal(
    @PrimaryKey
    val name: String, // Primärschlüssel
    val category: FoodCategoryEnum = FoodCategoryEnum.UNBEKANNT
)
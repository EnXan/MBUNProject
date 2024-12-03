package com.example.projektmbun.models.data.recipe

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projektmbun.models.data.food.Food

/**
 * Represents an ingredient along with its associated food details.
 * Combines information from the `Ingredient` entity and the related `Food` entity.
 *
 * @property ingredient The ingredient details from the `Ingredient` entity.
 * @property food The associated food details from the `Food` entity, linked by the `name` property.
 */
data class IngredientWithFood(
    @Embedded val ingredient: Ingredient,
    @Relation(
        parentColumn = "name",
        entityColumn = "name"
    )
    val food: Food
)

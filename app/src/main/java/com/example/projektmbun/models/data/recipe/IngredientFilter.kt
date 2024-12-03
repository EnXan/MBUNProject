package com.example.projektmbun.models.data.recipe

import com.example.projektmbun.utils.enums.UnitsEnum

/**
 * Represents a filter for ingredients, typically used for searching or filtering recipes
 * based on ingredient name, amount, and unit.
 *
 * @property name The name of the ingredient to filter by.
 * @property amount The quantity of the ingredient to filter by.
 * @property unit The unit of measurement for the ingredient's quantity, defined in `UnitsEnum`.
 */
data class IngredientFilter(
    val name: String,
    val amount: Double,
    val unit: UnitsEnum
)


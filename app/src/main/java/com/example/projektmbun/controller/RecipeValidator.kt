package com.example.projektmbun.controller

import android.content.Context

class RecipeValidator(
    private val recipeTempDataHandler: RecipeTempDataHandler,
    val context: Context
) {

    fun validateRecipeInfoFields(recipeTitle: String, recipePortions: String, shortDesc: String, prepTime: Int, recipeSrc: String): Boolean {
        var isValid = true

        if(recipeTempDataHandler.getRecipeData().imageUrl.isBlank()) {
            isValid = false
        }
        if (recipeTitle.isBlank()) {
            isValid = false
        }
        if (recipePortions.isBlank() || recipePortions.toIntOrNull() == null) {
            isValid = false
        }
        if (shortDesc.isBlank()) {
            isValid = false
        }
        if (prepTime == 0) {
            isValid = false
        }
        if (recipeSrc.isBlank()) {
            isValid = false
        }

        return isValid
    }

    fun validateIngredientFields(ingredientName: String, ingredientAmount: Int?, ingredientPrice: Double?): Boolean {
        var isValid = true

        if (ingredientName.isBlank()) {
            isValid = false
        }
        if (ingredientAmount == null || ingredientAmount <= 0 || ingredientAmount >=10000) {
            isValid = false
        }
        if (ingredientPrice == null || ingredientPrice <= 0 || ingredientPrice >= 999) {
            isValid = false
        }

        return isValid
    }

    fun validateInstructionFields(instructionDesc: String): Boolean {
        var isValid = true

        if (instructionDesc.isBlank()) {
            isValid = false
        }

        return isValid
    }

    fun validateIngredientExistence(): Boolean {
        return recipeTempDataHandler.getAllIngredients().isNotEmpty()
    }

    fun validateInstructionExistence(): Boolean {
        return recipeTempDataHandler.getAllInstructions().isNotEmpty()
    }
}
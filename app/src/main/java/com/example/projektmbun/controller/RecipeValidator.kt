package com.example.projektmbun.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.projektmbun.views.temp_data_models.TemporaryFood
import com.example.projektmbun.views.temp_data_models.TemporaryIngredient
import com.example.projektmbun.views.temp_data_models.TemporaryInstruction
import com.example.projektmbun.views.temp_data_models.TemporaryRecipe

class RecipeValidator(
    private val tempRecipe: TemporaryRecipe,
    private val ingredientsList: MutableList<Pair<TemporaryFood, TemporaryIngredient>>,
    private val instructionsList: MutableList<TemporaryInstruction>,
    val context: Context) {

    fun validateRecipeInfoFields(recipeTitle: String, recipePortions: String, shortDesc: String, prepTime: Int, recipeSrc: String): Boolean {
        var isValid = true

        if(tempRecipe.imageUrl.isBlank()) {
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
        return ingredientsList.isNotEmpty()
    }

    fun validateInstructionExistence(): Boolean {
        return instructionsList.isNotEmpty()
    }
}
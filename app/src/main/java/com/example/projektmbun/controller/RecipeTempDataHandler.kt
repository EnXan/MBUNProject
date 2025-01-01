package com.example.projektmbun.controller

import android.util.Log
import com.example.projektmbun.views.temp_data_models.*
import com.example.projektmbun.utils.enums.DifficultyEnum
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum

class RecipeTempDataHandler {
    private val tempRecipe = TemporaryRecipe()
    private val ingredientsList = mutableListOf<Pair<TemporaryFood, TemporaryIngredient>>()
    private val instructionsList = mutableListOf<TemporaryInstruction>()
    private val instructionsWithEquipments = mutableMapOf<TemporaryInstruction, List<TemporaryEquipment>>()
    private var tempInstructionImageUrl: String? = null

    // Recipe Methods
    fun updateRecipeBasicInfo(
        title: String,
        servings: Int,
        dishType: String,
        shortDesc: String,
        prepTime: Int,
        cookTime: Int,
        recipeSrc: String,
    ) {
        tempRecipe.apply {
            this.title = title
            this.servings = servings
            this.preparationMinutes = prepTime
            this.dishType = dishType
            this.cookingMinutes = cookTime
            this.readyInMinutes = prepTime + cookTime
            this.shortDescription = shortDesc
            this.sourceUrl = recipeSrc
            this.pricePerServing = calculatePricePerServing() // Update price when basic info changes
        }
    }

    fun updateRecipeDifficulty(difficulty: DifficultyEnum) {
        tempRecipe.difficulty = difficulty
    }

    fun updateRecipeDietaryInfo(
        isDairyFree: Boolean,
        isGlutenFree: Boolean,
        isNutFree: Boolean,
        isVegan: Boolean,
        isVegetarian: Boolean,
        isPescetarian: Boolean
    ) {
        tempRecipe.apply {
            dairyFree = isDairyFree
            glutenFree = isGlutenFree
            nutFree = isNutFree
            vegan = isVegan
            vegetarian = isVegetarian
            pescetarian = isPescetarian
        }
    }

    private fun calculatePricePerServing(): Double {
        val totalPrice = calculateTotalPrice()
        return if (tempRecipe.servings > 0) {
            totalPrice / tempRecipe.servings
        } else {
            totalPrice
        }
    }

    fun updateRecipeImage(imageUrl: String) {
        tempRecipe.imageUrl = imageUrl
    }

    // Ingredient Methods
    fun addIngredient(
        name: String,
        category: FoodCategoryEnum,
        description: String,
        amount: Double,
        unit: UnitsEnum,
        price: Double,
        isOptional: Boolean
    ): Int {
        val newFood = TemporaryFood(name, category)
        val newIngredient = TemporaryIngredient(
            description = description,
            amount = amount,
            unit = unit,
            price = price,
            isOptional = isOptional
        )
        ingredientsList.add(Pair(newFood, newIngredient))
        updatePrices()
        Log.d("RecipeTempDataHandler", "Added ingredient: ${newFood.name} (${ingredientsList.size} total)")
        return ingredientsList.size - 1
    }

    fun updateIngredient(
        index: Int,
        name: String,
        category: FoodCategoryEnum,
        description: String,
        amount: Double,
        unit: UnitsEnum,
        price: Double,
        isOptional: Boolean
    ): Boolean {
        if (index < 0 || index >= ingredientsList.size) return false

        val newFood = TemporaryFood(name, category)
        val newIngredient = TemporaryIngredient(
            description = description,
            amount = amount,
            unit = unit,
            price = price,
            isOptional = isOptional
        )
        ingredientsList[index] = Pair(newFood, newIngredient)
        updatePrices()
        Log.d("RecipeTempDataHandler", "Updated ingredient at index $index: ${newFood.name}")
        return true
    }

    fun removeIngredient(index: Int): Boolean {
        if (index < 0 || index >= ingredientsList.size) return false
        val removed = ingredientsList.removeAt(index)
        updatePrices()
        Log.d("RecipeTempDataHandler", "Removed ingredient: ${removed.first.name}")
        return true
    }

    fun getIngredient(index: Int): Pair<TemporaryFood, TemporaryIngredient>? {
        return ingredientsList.getOrNull(index)
    }

    fun getIngredientDisplay(index: Int): Triple<String, Int, String>? {
        val ingredient = getIngredient(index) ?: return null
        val amount = ingredient.second.amount ?: return null
        return Triple(
            ingredient.first.name,
            amount.toInt(),
            ingredient.second.unit.toString()
        )
    }

    // Instruction Methods
    fun addInstruction(
        description: String,
        imageUrl: String?,
        equipment: List<TemporaryEquipment>
    ): Int {
        val stepNumber = instructionsList.size + 1
        val newInstruction = TemporaryInstruction(
            step = stepNumber,
            description = description,
            imageUrl = imageUrl
        )
        instructionsList.add(newInstruction)
        instructionsWithEquipments[newInstruction] = equipment
        Log.d("TempHandler", "instructionList (add): $instructionsList")
        return instructionsList.size - 1
    }

    fun updateInstruction(
        index: Int,
        description: String,
        imageUrl: String?,
        equipment: List<TemporaryEquipment>
    ): Boolean {
        if (index < 0 || index >= instructionsList.size) return false

        val updatedInstruction = TemporaryInstruction(
            step = index + 1,
            description = description,
            imageUrl = imageUrl
        )

        val oldInstruction = instructionsList[index]
        instructionsList[index] = updatedInstruction
        instructionsWithEquipments.remove(oldInstruction)
        instructionsWithEquipments[updatedInstruction] = equipment
        Log.d("TempHandler", "instructionList (update): $instructionsList")
        return true
    }

    fun removeInstruction(index: Int): Boolean {
        if (index < 0 || index >= instructionsList.size) return false
        val instruction = instructionsList.removeAt(index)
        instructionsWithEquipments.remove(instruction)

        // Update step numbers for remaining instructions
        instructionsList.forEachIndexed { i, inst ->
            instructionsList[i] = inst.copy(step = i + 1)
        }
        Log.d("TempHandler", "instructionList (delete): $instructionsList")
        return true
    }

    fun getInstruction(index: Int): Pair<TemporaryInstruction, List<TemporaryEquipment>>? {
        val instruction = instructionsList.getOrNull(index) ?: return null
        val equipment = instructionsWithEquipments[instruction] ?: emptyList()
        return Pair(instruction, equipment)
    }

    // Temporary Instruction Image Methods
    fun setTempInstructionImage(imageUrl: String) {
        tempInstructionImageUrl = imageUrl
    }

    fun getTempInstructionImage(): String? {
        return tempInstructionImageUrl
    }

    fun clearTempInstructionImage() {
        tempInstructionImageUrl = null
    }

    // Data Access Methods
    fun getRecipeData() = tempRecipe
    fun getAllIngredients() = ingredientsList.toList()
    fun getAllInstructions() = instructionsList.toList()
    fun getAllEquipment() = instructionsWithEquipments.toMap()
    fun getIngredientsCount() = ingredientsList.size
    fun getInstructionsCount() = instructionsList.size

    // Price Calculation
    fun calculateTotalPrice(): Double {
        return ingredientsList.sumOf { it.second.price }
    }

    private fun updatePrices() {
        tempRecipe.pricePerServing = calculatePricePerServing()
    }


    // Reset Methods
    fun clearAll() {
        ingredientsList.clear()
        instructionsList.clear()
        instructionsWithEquipments.clear()
        tempInstructionImageUrl = null
        updatePrices()
    }
}
package com.example.projektmbun.models.cloud.service

import android.util.Log
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.data_structure.recipe.Equipment
import com.example.projektmbun.models.data_structure.recipe.Ingredient
import com.example.projektmbun.models.data_structure.recipe.Instructions
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.models.data_structure.supabase_returns.InsertedInstructionId
import com.example.projektmbun.models.data_structure.supabase_returns.InsertedRecipeId
import com.example.projektmbun.models.database.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeService {

    private val recipesTable = "recipes"
    private val ingredientsTable = "ingredients"
    private val foodTable = "food"
    private val instructionsTable = "instructions"
    private val equipmentTable = "equipment"

    suspend fun insertFullRecipe(
        recipe: Recipe,
        ingredients: List<Ingredient>,
        foodLocal: List<FoodLocal>,
        instructions: List<Instructions>,
        equipment: List<Equipment>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Rezept einfügen
            val insertedRecipe = supabase.from(recipesTable).insert(recipe) {
                select(columns = Columns.list("id"))
            }.decodeSingle<InsertedRecipeId>()

            val recipeId = insertedRecipe.id
            if (recipeId == null) {
                Log.e("RecipeService", "Fehler: Rezept wurde eingefügt, aber keine ID erhalten.")
                return@withContext false
            }

            val foodNames = foodLocal.map { it.name }
            val existingFoods = supabase.from(foodTable)
                .select(columns = Columns.list("name")) {
                    filter {
                        isIn("name", foodNames)
                    }
                }.decodeList<FoodLocal>()

            val foodsToInsert = foodLocal.filter { food ->
                existingFoods.none { it.name == food.name }
            }

            if (foodsToInsert.isNotEmpty()) {
                supabase.from(foodTable).insert(foodsToInsert) {
                    select(columns = Columns.list("name"))
                }

            } else {
                Log.d("RecipeService", "Alle Foods existieren bereits, kein Einfügen erforderlich.")
            }

// Erstelle eine Map von Food-Namen zu Food-Objekten
            val foodMap = foodLocal.associateBy { it.name }

// Ingredients mit Rezept- und Food-Referenz verknüpfen
            val preparedIngredients = ingredients.map { ingredient ->
                val foodName = ingredient.foodId
                val food = foodMap[foodName] ?: throw Exception("Food mit dem Namen $foodName nicht gefunden")
                ingredient.copy(
                    recipeId = recipeId,
                    foodId = food.name // Da 'name' der Primärschlüssel ist
                )
            }

            if (preparedIngredients.isNotEmpty()) {
                supabase.from(ingredientsTable).insert(preparedIngredients) {
                    select(columns = Columns.list("foodId"))
                }
                Log.d("RecipeService", "Zutaten erfolgreich hinzugefügt.")
            } else {
                Log.d("RecipeService", "Keine Zutaten zum Einfügen vorhanden.")
            }


            // Instructions mit Rezept verknüpfen
            val preparedInstructions = instructions.map { it.copy(recipeId = recipeId) }
            val insertedInstructions = if (preparedInstructions.isNotEmpty()) {
                supabase.from(instructionsTable).insert(preparedInstructions) {
                    select(columns = Columns.list("id"))
                }.decodeList<InsertedInstructionId>()
            } else {
                emptyList()
            }
            Log.d("RecipeService", "Anweisungen erfolgreich hinzugefügt: ${insertedInstructions.size}")

            // Equipment mit den Anweisungen verknüpfen
            if (insertedInstructions.isNotEmpty()) {
                val preparedEquipment = insertedInstructions.flatMap { instruction ->
                    equipment.map { it.copy(instructionId = instruction.id) }
                }
                if (preparedEquipment.isNotEmpty()) {
                    supabase.from(equipmentTable).insert(preparedEquipment) {
                        select(columns = Columns.list("id"))
                    }
                    Log.d("RecipeService", "Geräte erfolgreich hinzugefügt.")
                } else {
                    Log.d("RecipeService", "Keine Geräte zum Einfügen vorhanden.")
                }
            } else {
                Log.d("RecipeService", "Keine Anweisungen vorhanden, daher keine Geräte eingefügt.")
            }

            Log.d("RecipeService", "Rezept und alle zugehörigen Daten erfolgreich eingefügt.")
            return@withContext true

        } catch (e: Exception) {
            Log.e("RecipeService", "Fehler beim Einfügen des Rezepts: ${e.localizedMessage}")
            e.printStackTrace()
            return@withContext false
        }
    }


    suspend fun removeRecipeByTitle(title: String) = withContext(Dispatchers.IO) {
        try {
            supabase.from(recipesTable)
                .delete {
                    filter {
                    eq("title", title)
                    }
                }
        } catch (e: Exception) {
            throw e
        }
    }


    suspend fun getRecipeById(recipeId: Int): Recipe? = withContext(Dispatchers.IO) {
        val columns = Columns.raw("""
        id,
        title,
        dishType,
        imageUrl,
        shortDescription,
        servings,
        readyInMinutes,
        cookingMinutes,
        preparationMinutes,
        sourceUrl,
        difficulty,
        pricePerServing,
        dairyFree,
        glutenFree,
        nutFree,
        vegan,
        vegetarian,
        pescetarian,
        popularityScore
    """.trimIndent())

        try {
            supabase.from(recipesTable)
                .select(columns = columns) {
                    filter {
                        eq("id", recipeId)
                    }
                }
                .decodeSingle<Recipe>()
        } catch (e: Exception) {
            Log.e("RecipeService", "Fehler beim Abrufen des Rezepts: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getIngredientsByRecipeId(recipeId: Int): List<Ingredient> = withContext(Dispatchers.IO) {
        val columns = Columns.raw("""
        id,
        recipeId,
        foodId,
        description,
        price,
        amount,
        unit,
        isOptional
    """.trimIndent())

        Log.d("RecipeService", "columns for ingredients are: $columns")

        return@withContext try {
            val result = supabase.from(ingredientsTable)
                .select(columns = columns) {
                    filter {
                        eq("recipeId", recipeId)
                    }
                }
                .decodeList<Ingredient>()

            // Logge die decodeList-Ergebnisse
            Log.d("RecipeService", "decodeList: $result")

            result
        } catch (e: Exception) {
            Log.e("RecipeService", "Fehler beim Abrufen der Zutaten: ${e.localizedMessage}")
            emptyList()
        }

    }

    suspend fun getInstructionsByRecipeId(recipeId: Int): List<Instructions> = withContext(Dispatchers.IO) {
        val columns = Columns.raw("""
        id,
        step,
        imageUrl,
        description,
        recipeId
    """.trimIndent())

        try {
            supabase.from(instructionsTable)
                .select(columns = columns) {
                    filter {
                        eq("recipeId", recipeId)
                    }
                }
                .decodeList<Instructions>()
        } catch (e: Exception) {
            Log.e("RecipeService", "Fehler beim Abrufen der Anweisungen: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getRecipesByTitle(name: String): List<Recipe> = withContext(Dispatchers.IO) {
        val columns = Columns.raw("""
        id,
        title,
        dishType,
        imageUrl,
        shortDescription,
        servings,
        readyInMinutes,
        cookingMinutes,
        preparationMinutes,
        sourceUrl,
        difficulty,
        pricePerServing,
        dairyFree,
        glutenFree,
        nutFree,
        vegan,
        vegetarian,
        pescetarian,
        popularityScore
    """.trimIndent())

        try {
            supabase.from(recipesTable)
                .select(columns = columns) {
                    filter {
                        eq("title", name)
                    }
                }
                .decodeList<Recipe>()
        } catch (e: Exception) {
            Log.e("RecipeService", "Fehler beim Abrufen der Rezepte: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun getAllAvailableRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        val columns = Columns.raw("""
        id,
        title,
        dishType,
        imageUrl,
        shortDescription,
        servings,
        readyInMinutes,
        cookingMinutes,
        preparationMinutes,
        sourceUrl,
        difficulty,
        pricePerServing,
        dairyFree,
        glutenFree,
        nutFree,
        vegan,
        vegetarian,
        pescetarian,
        popularityScore
    """.trimIndent())

        try {
            val result = supabase.from(recipesTable)
                .select(columns = columns)
                .decodeList<Recipe>()
            return@withContext result
        } catch (e: Exception) {
            emptyList()
        }
    }
}

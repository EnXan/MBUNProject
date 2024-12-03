package com.example.projektmbun.models.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.projektmbun.models.data.recipe.RecipeWithDishTypesAndIngredientsAndInstructions

/**
 * Data Access Object (DAO) interface for performing database operations on the `Recipe` entity.
 * Provides methods for retrieving recipes with related details such as dish types, ingredients,
 * and instructions. Supports queries by recipe ID, name, and ingredient matching.
 */
@Dao
interface RecipeDao {

    /**
     * Get all recipes with their dish types, ingredients, and instructions.
     * @return a list of recipes with their dish types, ingredients, and instructions or empty list `List<RecipeWithDishTypesAndIngredientsAndInstructions>`.
     */
    @Transaction
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): List<RecipeWithDishTypesAndIngredientsAndInstructions>

    /**
     * Get a recipe by its id with its dish types, ingredients, and instructions.
     * @param recipeId the id of the recipe to get.
     * @return a recipe with its dish types, ingredients, and instructions `RecipeWithDishTypesAndIngredientsAndInstructions` or null if not found `null`.
     */
    @Transaction
    @Query("""
        SELECT * FROM recipes
        WHERE id = :recipeId
    """)
    fun getRecipeById(recipeId: Int): RecipeWithDishTypesAndIngredientsAndInstructions?

    /**
     * Get a recipe by its ingredient names with its dish types, ingredients, and instructions.
     * @param ingredientNames a list of ingredient names to search for.
     * @param ingredientCount the minimum number of ingredients to match.
     * @return a list of recipes with their dish types, ingredients, and instructions or empty list `List<RecipeWithDishTypesAndIngredientsAndInstructions>`.
     */
    @Transaction
    @Query("""
    SELECT * FROM recipes
    WHERE id IN (
        SELECT recipeId FROM ingredients
        WHERE name IN (:ingredientNames)
        GROUP BY recipeId
        HAVING COUNT(DISTINCT name) >= :ingredientCount
    )
""")
    fun getRecipesByIngredientNames(
        ingredientNames: List<String>,
        ingredientCount: Int
    ): List<RecipeWithDishTypesAndIngredientsAndInstructions>


    /**
     * Get recipes by most matching name with dish types, ingredients, and instructions.
     * @param name the name of the recipes to get.
     * @return a list of recipes with its dish types, ingredients, and instructions or empty list `List<RecipeWithDishTypesAndIngredientsAndInstructions>`.
     */
    @Transaction
    @Query("""
    SELECT * FROM recipes
    WHERE title LIKE '%' || :name || '%'
""")
    fun getRecipesByName(name: String): List<RecipeWithDishTypesAndIngredientsAndInstructions>
}

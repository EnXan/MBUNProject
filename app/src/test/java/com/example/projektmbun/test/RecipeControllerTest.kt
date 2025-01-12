package com.example.projektmbun.test

import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.recipe.Ingredient
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.utils.enums.DifficultyEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import com.example.projektmbun.views.temp_data_models.TemporaryRecipe
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull

class RecipeControllerTest {

    private lateinit var recipeService: RecipeService
    private lateinit var recipeController: RecipeController

    @Before
    fun setUp() {
        recipeService = mock(RecipeService::class.java)
        recipeController = RecipeController(recipeService)
    }

    @Test
    fun `test addRecipeWithDetails success`() = runBlocking {
        val recipe = TemporaryRecipe(
            title = "Test Recipe",
            dishType = "Main",
            imageUrl = "http://example.com/image.jpg",
            shortDescription = "A test recipe",
            servings = 4,
            readyInMinutes = 30,
            cookingMinutes = 20,
            preparationMinutes = 10,
            sourceUrl = "http://example.com",
            difficulty = DifficultyEnum.EASY,
            pricePerServing = 5.0,
            dairyFree = true,
            glutenFree = false,
            nutFree = true,
            vegan = false,
            vegetarian = true,
            pescetarian = false,
            popularityScore = 10.0
        )

        `when`(recipeService.insertFullRecipe(
            anyOrNull(), anyList(), anyList(), anyList(), anyList()))
            .thenReturn(true)

        val result = recipeController.addRecipeWithDetails(
            recipe, emptyList(), emptyList(), emptyList(), emptyList())
        assertTrue(result)
    }

    @Test
    fun `test getRecipeById returns recipe`() = runBlocking {
        val recipeId = 1
        val expectedRecipe = Recipe(
            id = recipeId,
            title = "Test Recipe",
            dishType = "Main",
            imageUrl = "http://example.com/image.jpg",
            shortDescription = "A test recipe",
            servings = 4,
            readyInMinutes = 30,
            cookingMinutes = 20,
            preparationMinutes = 10,
            sourceUrl = "http://example.com",
            difficulty = DifficultyEnum.EASY,
            pricePerServing = 5.0,
            dairyFree = true,
            glutenFree = false,
            nutFree = true,
            vegan = false,
            vegetarian = true,
            pescetarian = false,
            popularityScore = 10.0
        )

        `when`(recipeService.getRecipeById(recipeId)).thenReturn(expectedRecipe)

        val result = recipeController.getRecipeById(recipeId)
        assertEquals(expectedRecipe, result)
    }

    @Test
    fun `test getFilteredRecipes returns correct recipes`() = runBlocking {
        val foodCards = listOf(FoodCard(foodId = "Apple", quantity = 2.0, unit = UnitsEnum.STUECK, expiryDate = null, isActive = true))
        val recipes = listOf(
            Recipe(id = 1, title = "Apple Pie", dishType = "Dessert", imageUrl = "", shortDescription = "", servings = 4, readyInMinutes = 60, cookingMinutes = 45, preparationMinutes = 15, sourceUrl = "", difficulty = DifficultyEnum.MEDIUM, pricePerServing = 3.0, dairyFree = false, glutenFree = false, nutFree = true, vegan = false, vegetarian = true, pescetarian = false, popularityScore = 8.0)
        )

        `when`(recipeService.getAllAvailableRecipes()).thenReturn(recipes)
        `when`(recipeService.getIngredientsByRecipeId(1)).thenReturn(listOf(Ingredient(foodId = "Apple", amount = 2.0, unit = UnitsEnum.STUECK, description = "", price = 1.0, isOptional = false)))

        val result = recipeController.getFilteredRecipes(foodCards, 0)
        assertEquals(1, result.size)
        assertEquals("Apple Pie", result[0].title)
    }

    @Test
    fun `test addRecipeWithDetails fails with invalid data`() = runBlocking {
        val invalidRecipe = TemporaryRecipe(
            title = "", // Invalid title
            dishType = "Main",
            imageUrl = "http://example.com/image.jpg",
            shortDescription = "A test recipe",
            servings = 4,
            readyInMinutes = 30,
            cookingMinutes = 20,
            preparationMinutes = 10,
            sourceUrl = "http://example.com",
            difficulty = DifficultyEnum.EASY,
            pricePerServing = 5.0,
            dairyFree = true,
            glutenFree = false,
            nutFree = true,
            vegan = false,
            vegetarian = true,
            pescetarian = false,
            popularityScore = 10.0
        )

        `when`(recipeService.insertFullRecipe(anyOrNull(), anyList(), anyList(), anyList(), anyList())).thenReturn(false)

        val result = recipeController.addRecipeWithDetails(invalidRecipe, emptyList(), emptyList(), emptyList(), emptyList())
        assertFalse(result)
    }

    @Test
    fun `test getRecipeById throws IllegalArgumentException for invalid ID`() = runBlocking {
        val invalidRecipeId = -1

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                recipeController.getRecipeById(invalidRecipeId)
            }
        }

        assertEquals("Ungültige Rezept-ID: -1", exception.message)
    }

    @Test
    fun `test getFilteredRecipes returns empty list for empty ingredients`() = runBlocking {
        val foodCards = emptyList<FoodCard>()

        `when`(recipeService.getAllAvailableRecipes()).thenReturn(emptyList())

        val result = recipeController.getFilteredRecipes(foodCards, 0)
        assertTrue(result.isEmpty())
    }
}
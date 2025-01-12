package com.example.projektmbun.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.enums.DifficultyEnum
import com.example.projektmbun.views.temp_data_models.TemporaryRecipe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RecipeIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var recipeController: RecipeController
    private lateinit var recipeService: RecipeService

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        recipeService = RecipeService()
        recipeController = RecipeController(recipeService)
    }

    @After
    @Throws(IOException::class)
    fun closeDbAndRemoveRecipe() {
        runBlocking {
        recipeController.removeRecipeByTitle("Integration Test Recipe")
        }
        db.close()
    }

    @Test
    fun testAddAndRetrieveRecipe() = runBlocking {
        val recipe = TemporaryRecipe(
            title = "Integration Test Recipe",
            dishType = "Dessert",
            imageUrl = "http://example.com/image.jpg",
            shortDescription = "A test recipe for integration",
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

        // Add the recipe
        val success = recipeController.addRecipeWithDetails(
            recipe = recipe,
            ingredients = emptyList(),
            instructions = emptyList(),
            equipment = emptyList(),
            food = emptyList()
        )
        assertTrue("Recipe should be added successfully", success)

        // Retrieve the recipe by title
        val retrievedRecipes = recipeController.getRecipesByTitle("Integration Test Recipe")
        assertTrue("Retrieved recipes should not be empty", retrievedRecipes.isNotEmpty())
        assertEquals("Retrieved recipe title should match", "Integration Test Recipe", retrievedRecipes[0].title)
    }
} 
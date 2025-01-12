package com.example.projektmbun.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay

@RunWith(AndroidJUnit4::class)
class FoodCardIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var foodCardController: FoodCardController
    private lateinit var foodService: FoodService

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        // Mock the FoodService
        foodService = mockk()
        // Mock both singular and plural versions
        coEvery { foodService.getFoodByName("Test Apple") } returns listOf(FoodLocal(name = "Test Apple", category = FoodCategoryEnum.OBST))
        coEvery { foodService.getFoodsByNames(any()) } returns listOf(FoodLocal(name = "Test Apple", category = FoodCategoryEnum.OBST))

        // Insert test food into database
        runBlocking {
            db.foodDao().insertFood(FoodLocal(name = "Test Apple", category = FoodCategoryEnum.OBST))
        }

        foodCardController = FoodCardController(db.foodCardDao(), foodService)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAddAndRetrieveFoodCard() = runBlocking<Unit> {
        // First verify that the Food exists in the database
        val existingFood = db.foodDao().getFoodByName("Test Apple")
        assertNotNull("Food should exist in database", existingFood)
        
        // Create a test FoodCard
        val testFoodCard = FoodCard(
            id = null,
            foodId = "Test Apple",
            quantity = 5.0,
            unit = UnitsEnum.STUECK,
            expiryDate = "31.12.2024",
            isActive = true,
            stockId = null,
            routineId = null
        )

        // Add the FoodCard and verify the ID
        val foodCardId = foodCardController.addFoodCard(testFoodCard)
        assertNotNull("FoodCard should be added successfully", foodCardId)
        
        // Verify the FoodCard exists directly in the database
        val dbFoodCard = db.foodCardDao().getFoodCardById(foodCardId)
        assertNotNull("FoodCard should exist in database", dbFoodCard)

        // Now try to retrieve it through the controller
        val foodCards = foodCardController.getFoodCardsWithDetails()
        assertTrue("FoodCards should not be empty", foodCards.isNotEmpty())

        // Verify the FoodCard properties
        val retrievedFoodCard = foodCards.find { it.foodCard.id == foodCardId }
        assertNotNull("Should find the added FoodCard", retrievedFoodCard)
        retrievedFoodCard?.let {
            assertEquals("Food ID should match", "Test Apple", it.foodCard.foodId)
            assertEquals("Quantity should match", 5.0, it.foodCard.quantity, 0.0)
            assertEquals("Unit should match", UnitsEnum.STUECK, it.foodCard.unit)
            assertEquals("Expiry date should match", "31.12.2024", it.foodCard.expiryDate)
            assertTrue("FoodCard should be active", it.foodCard.isActive)
        }
    }
} 
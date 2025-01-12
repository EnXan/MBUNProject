package com.example.projektmbun.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.stock.Stock
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

@RunWith(AndroidJUnit4::class)
class  FoodIntegrationTest {
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

        foodService = FoodService()
        foodCardController = FoodCardController(db.foodCardDao(), foodService)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testFoodStockIsolation() = runBlocking {

        val stock = Stock(id = 1, name = "Test Stock")
        val stockId = db.stockDao().createStock(stock)
        println("Inserted Stock ID: $stockId")

        val insertedStock = db.stockDao().getStockIdByName("Test Stock")
        if(insertedStock == 1 ) {
            println("Stock is there!")
        }

        // 1. Create test data
        val testFood = FoodLocal(
            name = "Test Apple",
            category = FoodCategoryEnum.OBST
        )

        db.foodDao().insertFood(testFood)

        val testFoodCard = FoodCard(
            id = null,
            foodId = testFood.name,
            quantity = 1.0,
            unit = UnitsEnum.STUECK,
            expiryDate = "2024-12-31",
            isActive = true,
            stockId = 1, // Assign to stock
            routineId = null // Should remain null
        )

        // 2. Add food card to stock
        val addedFoodCardId = foodCardController.addFoodCardToStock(testFoodCard, 1)
        assertNotNull("Food card should be added successfully", addedFoodCardId)

        // 3. Verify food card exists in stock
        val foodCardsInStock = foodCardController.getFoodCardsInStock()
        println("Food Cards in Stock: " + foodCardsInStock)
        assertFalse("Stock should not be empty", foodCardsInStock.isEmpty())

        // 4. Verify the food card in stock has correct properties
        val storedFoodCard = foodCardsInStock.find { it.foodCard.foodId == testFood.name }
        assertNotNull("Should find the added food card", storedFoodCard)
        storedFoodCard?.let {
            assertEquals("Stock ID should be 1", 1, it.foodCard.stockId)
            assertNull("Routine ID should be null", it.foodCard.routineId)
            assertTrue("Food card should be active", it.foodCard.isActive)
            assertEquals("Food category should match",
                FoodCategoryEnum.OBST,
                it.foodLocal.category)
        }

        // 5. Verify no unintended connections exist
        val allFoodCards = foodCardController.getFoodCardsWithDetails()
        val foodCardsWithRoutines = allFoodCards.filter { it.foodCard.routineId != null }
        assertTrue("No food cards should be connected to routines",
            foodCardsWithRoutines.isEmpty())
    }
}
package com.example.projektmbun.test

import android.database.sqlite.SQLiteConstraintException
import com.example.projektmbun.controller.StockController
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.local.daos.StockDao
import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30]) // Optional: Android API Level konfigurieren
class AddFoodUnitTest {
    @Mock
    private lateinit var stockDao: StockDao

    @Mock
    private lateinit var foodCardDao: FoodCardDao

    private lateinit var stockController: StockController

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stockController = StockController(stockDao, foodCardDao)
    }

    @Test
    fun `test adding food card to stock`(): Unit = runBlocking {
        // Arrange
        val foodCard = FoodCard(
            id = null,
            foodId = "Apple",
            quantity = 5.0,
            unit = UnitsEnum.STUECK,
            expiryDate = LocalDate.now().plusDays(7).toString(),
            isActive = true,
            stockId = 1
        )

        whenever(foodCardDao.insertFoodCard(any())).thenReturn(1L)

        // Act
        val result = stockController.addFoodCardToStock(foodCard, 1)

        // Assert
        println("Test: Adding food card to stock")
        println("Expected: 1L, Actual: $result")
        assertNotNull(result)
        assertEquals(1L, result)
        verify(foodCardDao).insertFoodCard(any())
    }

    @Test
    fun `test adding food card to stock without expiry date`(): Unit = runBlocking {
        // Arrange
        val foodCard = FoodCard(
            id = null,
            foodId = "Banana",
            quantity = 3.0,
            unit = UnitsEnum.STUECK,
            expiryDate = null,
            isActive = true,
            stockId = 1
        )

        whenever(foodCardDao.insertFoodCard(any())).thenReturn(2L)

        // Act
        val result = stockController.addFoodCardToStock(foodCard, 1)

        // Assert
        println("Test: Adding food card to stock without expiry date")
        println("Expected: 2L, Actual: $result")
        assertNotNull(result)
        assertEquals(2L, result)
        verify(foodCardDao).insertFoodCard(any())
    }

    @Test
    fun `test adding food card with invalid data should return null`(): Unit = runBlocking {
        // Arrange
        val foodCard = FoodCard(
            id = null,
            foodId = "",  // Invalid food ID
            quantity = -1.0,  // Invalid quantity
            unit = UnitsEnum.STUECK,
            expiryDate = null,
            isActive = true,
            stockId = 1
        )

        whenever(foodCardDao.insertFoodCard(any())).thenThrow(IllegalArgumentException())

        // Act
        val result = stockController.addFoodCardToStock(foodCard, 1)

        // Assert
        println("Test: Adding food card with invalid data")
        println("Expected: null, Actual: $result")
        assertNull(result)
        verify(foodCardDao).insertFoodCard(any())
    }

    @Test
    fun `test adding food card without stock ID`(): Unit = runBlocking {
        // Arrange
        val foodCard = FoodCard(
            id = null,
            foodId = "Orange",
            quantity = 4.0,
            unit = UnitsEnum.STUECK,
            expiryDate = LocalDate.now().plusDays(5).toString(),
            isActive = true,
            stockId = null
        )

        whenever(foodCardDao.insertFoodCard(anyOrNull())).thenReturn(3L)

        // Act
        val result = stockController.addFoodCardToStock(foodCard, null)

        // Assert
        println("Test: Adding food card without stock ID")
        println("Expected: 3L, Actual: $result")
        assertNotNull(result)
        assertEquals(3L, result)
        verify(foodCardDao).insertFoodCard(anyOrNull())
    }

    @Test
    fun `test adding food card with SQLite constraint violation`(): Unit = runBlocking {
        // Arrange
        val foodCard = FoodCard(
            id = null,
            foodId = "Apple",
            quantity = 2.0,
            unit = UnitsEnum.STUECK,
            expiryDate = null,
            isActive = true,
            stockId = 1
        )

        whenever(foodCardDao.insertFoodCard(any())).thenThrow(SQLiteConstraintException())

        // Act
        val result = stockController.addFoodCardToStock(foodCard, 1)

        // Assert
        println("Test: Adding food card with SQLite constraint violation")
        println("Expected: null, Actual: $result")
        assertNull(result)
        verify(foodCardDao).insertFoodCard(any())
    }
}

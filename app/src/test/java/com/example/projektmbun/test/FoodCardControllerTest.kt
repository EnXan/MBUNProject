package com.example.projektmbun.test

import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.exceptions.FoodCardCreationException
import com.example.projektmbun.exceptions.FoodCardUpdateException
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import java.time.LocalDate
import android.database.sqlite.SQLiteConstraintException
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

class  FoodCardControllerTest {

    private lateinit var foodCardDao: FoodCardDao
    private lateinit var foodService: FoodService
    private lateinit var foodCardController: FoodCardController

    @Before
    fun setUp() {
        foodCardDao = mock(FoodCardDao::class.java)
        foodService = mock(FoodService::class.java)
        foodCardController = FoodCardController(foodCardDao, foodService)
    }

    @Test
    fun `test addFoodCard with valid food`(): Unit = runBlocking {
        val foodCard = FoodCard(
            id = null,
            foodId = "Test Apple",
            quantity = 1.0,
            unit = UnitsEnum.STUECK,
            expiryDate = "2024-12-31",
            isActive = true,
            stockId = null,
            routineId = null
        )

        `when`(foodService.getFoodByName("Test Apple")).thenReturn(listOf(FoodLocal(
            "Test Apple", FoodCategoryEnum.OBST))
        )
        `when`(foodCardDao.insertFoodCard(foodCard)).thenReturn(1L)

        foodCardController.addFoodCard(foodCard)

        verify(foodCardDao).insertFoodCard(foodCard)
    }

    @Test(expected = FoodCardCreationException::class)
    fun `test addFoodCard with invalid food`(): Unit = runBlocking {
        val foodCard = FoodCard(
            id = null,
            foodId = "Nonexistent Food",
            quantity = 1.0,
            unit = UnitsEnum.STUECK,
            expiryDate = "31.12.2024",
            isActive = true,
            stockId = null,
            routineId = null
        )

        whenever(foodService.getFoodByName("Nonexistent Food")).thenReturn(emptyList())

        foodCardController.addFoodCard(foodCard)
    }

    @Test
    fun `test updateExpiryDateByFoodCardId with valid date`(): Unit = runBlocking {
        val foodCardId = 1
        val newExpiryDate = "31.12.2024"

        `when`(foodCardDao.updateExpiryDateByFoodCardId(foodCardId, newExpiryDate)).
        thenReturn(1)

        foodCardController.updateExpiryDateByFoodCardId(foodCardId, newExpiryDate)

        verify(foodCardDao).updateExpiryDateByFoodCardId(foodCardId, newExpiryDate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test updateExpiryDateByFoodCardId with invalid date`(): Unit = runBlocking {
        val foodCardId = 1
        val invalidDate = "invalid-date"

        foodCardController.updateExpiryDateByFoodCardId(foodCardId, invalidDate)
    }

    @Test(expected = FoodCardUpdateException::class)
    fun `test updateExpiryDateByFoodCardId with no rows updated`(): Unit = runBlocking {
        val foodCardId = 1
        val newExpiryDate = "31.12.2024"

        `when`(foodCardDao.updateExpiryDateByFoodCardId(foodCardId, newExpiryDate)).thenReturn(0)

        foodCardController.updateExpiryDateByFoodCardId(foodCardId, newExpiryDate)
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

        whenever(foodService.getFoodByName("Apple"))
            .thenReturn(listOf(FoodLocal("Apple", FoodCategoryEnum.OBST)))
        whenever(foodCardDao.insertFoodCard(anyOrNull())).thenReturn(1L)

        // Act
        val result = foodCardController.addFoodCard(foodCard)

        // Assert
        println("Test: Adding food card to stock")
        println("Expected: 1, Actual: $result")
        assertNotNull(result)
        assertEquals(1, result)
        verify(foodCardDao).insertFoodCard(anyOrNull())
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

        whenever(foodService.getFoodByName("Banana"))
            .thenReturn(listOf(FoodLocal("Banana", FoodCategoryEnum.OBST)))
        whenever(foodCardDao.insertFoodCard(anyOrNull())).thenReturn(2L)

        // Act
        val result = foodCardController.addFoodCard(foodCard)

        // Assert
        println("Test: Adding food card to stock without expiry date")
        println("Expected: 2, Actual: $result")
        assertNotNull(result)
        assertEquals(2, result)
        verify(foodCardDao).insertFoodCard(anyOrNull())
    }

    @Test(expected = FoodCardCreationException::class)
    fun `test adding food card with invalid data should throw exception`(): Unit = runBlocking {
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

        whenever(foodService.getFoodByName(""))
            .thenReturn(emptyList())  // Simulate that the food does not exist

        // Act
        foodCardController.addFoodCard(foodCard)

        // Assert
        // No need for assertions as the test will pass if the exception is thrown
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

        whenever(foodService.getFoodByName("Orange"))
            .thenReturn(listOf(FoodLocal("Orange", FoodCategoryEnum.OBST)))
        whenever(foodCardDao.insertFoodCard(anyOrNull())).thenReturn(3L)

        // Act
        val result = foodCardController.addFoodCard(foodCard)

        // Assert
        println("Test: Adding food card without stock ID")
        println("Expected: 3, Actual: $result")
        assertNotNull(result)
        assertEquals(3, result)
        verify(foodCardDao).insertFoodCard(anyOrNull())
    }

    @Test(expected = FoodCardCreationException::class)
    fun `test adding food card with SQLite constraint violation should throw exception`(): Unit = runBlocking {
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

        whenever(foodService.getFoodByName("Apple"))
            .thenReturn(listOf(FoodLocal("Apple", FoodCategoryEnum.OBST)))
        whenever(foodCardDao.insertFoodCard(anyOrNull())).thenThrow(SQLiteConstraintException())

        // Act
        foodCardController.addFoodCard(foodCard)

        // Assert
        // No need for assertions as the test will pass if the exception is thrown
    }
}
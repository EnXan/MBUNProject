@file:Suppress("IllegalIdentifier")
package com.example.projektmbun.unittest
import com.example.projektmbun.controller.FoodController
import com.example.projektmbun.models.daos.FoodDao
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.FoodStateEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for FoodController.
 * This class contains unit tests for the methods in the FoodController class to verify its behavior.
 * It uses Mockito to create mocks for FoodDao and tests various scenarios for food retrieval.
 * The tests also cover edge cases like special characters, case sensitivity, and exception handling.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FoodControllerTest {

    //////////////////////////// TestSetup ////////////////////////////

    /**
     * Test dispatcher used to control coroutine execution.
     */
    private val testDispatcher = StandardTestDispatcher()

    /**
     * Instance of FoodController to be tested.
     */
    private lateinit var foodController: FoodController

    /**
     * Mock object for FoodDao used to simulate database interactions.
     */
    private lateinit var mockDao: FoodDao

    /**
     * Food object used for testing.
     */
    private lateinit var foodItem: Food

    /**
     * Sets up test environment before each test.
     * Initializes FoodController with mocked FoodDao and sets the main coroutine dispatcher.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockDao = mock(FoodDao::class.java)
        foodController = FoodController(foodDao = mockDao)
    }

    /**
     * Cleans up the test environment after each test.
     * Resets the main coroutine dispatcher to the original dispatcher.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    //////////////////////////// FoodControllerTest ////////////////////////////

    /**
     * Tests that the `getFoodByName` method returns the correct food list when a valid name is passed.
     */
    @Test
    suspend fun `getFoodByName() should return food list when valid name is passed`() {
        val foodList = listOf(
            Food("Apfel", FoodCategoryEnum.OBST, FoodStateEnum.SOLID),
            Food("Banane", FoodCategoryEnum.OBST, FoodStateEnum.SOLID)
        )

        whenever(mockDao.getFoodByName("Apfel")).thenReturn(foodList)

        val result = foodController.getFoodByName("Apfel")

        assertEquals(foodList, result)
    }

    /**
     * Tests that the `getFoodByName` method returns an empty list when no food matches the given name.
     */
    @Test
    suspend fun `getFoodByName() should return empty list when no food matches the name`() {
        whenever(mockDao.getFoodByName("NonExisting")).thenReturn(emptyList())

        val result = foodController.getFoodByName("NonExisting")

        assertTrue(result.isEmpty())
    }

    /**
     * Tests that the `getFoodByName` method returns the correct list when the food name contains leading or trailing spaces.
     */
    @Test
    suspend fun `getFoodByName() should return food list when food with space is passed`() {
        val foodList = listOf(
            Food("Apfel", FoodCategoryEnum.OBST, FoodStateEnum.SOLID)
        )

        whenever(mockDao.getFoodByName("Apfel")).thenReturn(foodList)

        val result = foodController.getFoodByName(" Apfel ")

        assertEquals(foodList, result)
    }

    /**
     * Tests that the `getFoodByName` method handles case sensitivity.
     * It should correctly return the food list even if the input name has a different case.
     */
    @Test
    suspend fun `getFoodByName() should handle case sensitivity in food name`() {
        val foodList = listOf(
            Food("Apfel", FoodCategoryEnum.OBST, FoodStateEnum.SOLID)
        )

        whenever(mockDao.getFoodByName("apfel")).thenReturn(foodList)

        val result = foodController.getFoodByName("apfel")

        assertEquals(foodList, result)
    }

    /**
     * Tests that the `getFoodByName` method handles special characters in food names.
     */
    @Test
    suspend fun `getFoodByName() should handle special characters in food name`() {
        val foodList = listOf(
            Food("Erdbeer-Joghurt", FoodCategoryEnum.MILCHPRODUKT, FoodStateEnum.SOLID)
        )

        whenever(mockDao.getFoodByName("Erdbeer-Joghurt")).thenReturn(foodList)

        val result = foodController.getFoodByName("Erdbeer-Joghurt")

        assertEquals(foodList, result)
    }

    /**
     * Tests that the `getFoodByName` method returns an empty list when an exception occurs.
     */
    @Test
    suspend fun `getFoodByName() should return an empty list when an exception is thrown`() {
        `when`(mockDao.getFoodByName("Apfel")).thenThrow(RuntimeException("Database error"))

        val result = foodController.getFoodByName("Apfel")

        assertEquals(emptyList<Food>(), result)
    }

    /**
     * Tests that the `getAllFood` method returns the complete list of food items when called.
     */
    @Test
    suspend fun `getAllFood() should return food list of all foods when called`() {
        val foodList = listOf(
            Food("Apfel", FoodCategoryEnum.OBST, FoodStateEnum.SOLID),
            Food("Banane", FoodCategoryEnum.OBST, FoodStateEnum.SOLID)
        )
        whenever(mockDao.getAllFood()).thenReturn(foodList)

        val result = foodController.getAllFood()

        assertEquals(foodList, result)
    }

    /**
     * Tests that the `getAllFood` method returns an empty list when an exception occurs.
     */
    @Test
    suspend fun `getAllFood() should return an empty list when an exception is thrown`() {
        `when`(mockDao.getAllFood()).thenThrow(RuntimeException("Database error"))

        val result = foodController.getAllFood()

        assertEquals(emptyList<Food>(), result)
    }

    /**
     * Tests that the `getFoodByCategory` method returns the correct list of foods for a given valid category.
     */
    @Test
    suspend fun `getFoodByCategory() should return food list of given valid category`() {
        val category = FoodCategoryEnum.OBST
        val expectedFoodList = listOf(
            Food("Apfel", category, FoodStateEnum.SOLID),
            Food("Banane", category, FoodStateEnum.SOLID)
        )
        whenever(mockDao.getFoodByCategory(category)).thenReturn(expectedFoodList)

        val result = foodController.getFoodByCategory(category)

        assertEquals(expectedFoodList, result)
    }

    /**
     * Tests that the `getFoodByCategory` method returns an empty list when no food items are in the given category.
     */
    @Test
    suspend fun `getFoodByCategory() should return empty list when no food in category`() {
        val category = FoodCategoryEnum.GEMUESE
        whenever(mockDao.getFoodByCategory(category)).thenReturn(emptyList())

        val result = foodController.getFoodByCategory(category)

        assertTrue(result.isEmpty())
    }

    /**
     * Tests that the `getFoodByCategory` method returns an empty list when an exception occurs.
     */
    @Test
    suspend fun `getFoodByCategory() should return empty list when exception is thrown`() {
        val category = FoodCategoryEnum.MILCHPRODUKT
        whenever(mockDao.getFoodByCategory(category)).thenThrow(RuntimeException("Database error"))

        val result = foodController.getFoodByCategory(category)

        assertTrue(result.isEmpty())
    }
}

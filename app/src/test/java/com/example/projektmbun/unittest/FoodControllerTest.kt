package com.example.projektmbun.unittest

import app.cash.turbine.test
import com.example.projektmbun.controller.FoodController
import com.example.projektmbun.models.daos.FoodDao
import com.example.projektmbun.models.data.Food
import com.example.projektmbun.models.data.FoodCategoryEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.sql.SQLException
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class FoodControllerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var foodController: FoodController
    private lateinit var mockDao: FoodDao
    private lateinit var foodItem: Food

    /**
     * Setting up mocks for foodController and foodDao
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockDao = mock(FoodDao::class.java)
        foodController = FoodController(foodDao = mockDao)
        foodItem = Food("Apfel", FoodCategoryEnum.OBST)


    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher to the original Main dispatcher
    }


    @Test
    fun testGetFoodByName() = runBlocking {
        val expectedFoods = listOf(Food("Apfel", FoodCategoryEnum.OBST), Food("Banane", FoodCategoryEnum.OBST))
        `when`(mockDao.getFoodsByName("Apfel")).thenReturn(flowOf(expectedFoods))

        // Act & Assert: Collecting the flow with Turbine for a precise flow test
        foodController.getFood("Apfel").test {
            assertEquals(expectedFoods, awaitItem())
            awaitComplete() // Confirms flow completes if applicable
        }
    }
}

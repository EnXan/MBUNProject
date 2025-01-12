package com.example.projektmbun.test

import android.database.sqlite.SQLiteConstraintException
import com.example.projektmbun.controller.FoodController
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.local.daos.FoodDao
import com.example.projektmbun.models.local.daos.StockDao
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import java.time.LocalDate

class FoodControllerTest {

    private lateinit var foodService: FoodService
    private lateinit var foodDao: FoodDao
    private lateinit var foodController: FoodController
    @Mock
    private lateinit var stockDao: StockDao
    @Mock
    private lateinit var foodCardDao: FoodCardDao

    @Before
    fun setUp() {
        foodService = mock(FoodService::class.java)
        foodDao = mock(FoodDao::class.java)
        foodController = FoodController(foodService, foodDao)
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test searchFoodByName returns correct food`() = runBlocking {
        val foodName = "Apple"
        val expectedFood = listOf(FoodLocal(name = foodName, category = FoodCategoryEnum.OBST))

        `when`(foodDao.getFoodByName(foodName)).thenReturn(expectedFood)

        val result = foodController.searchFoodByName(foodName)
        assertEquals(expectedFood, result)
    }

    @Test
    fun `test getAllFood returns all food items`() = runBlocking {
        val expectedFoodList = flowOf(listOf(
            FoodLocal(name = "Apple", category = FoodCategoryEnum.OBST),
            FoodLocal(name = "Carrot", category = FoodCategoryEnum.GEMUESE)
        ))

        `when`(foodDao.getAllFood()).thenReturn(expectedFoodList)

        val result = foodController.getAllFood()
        assertEquals(expectedFoodList, result)
    }

    @Test
    fun `test getFoodByCategory returns correct food items`() = runBlocking {
        val category = FoodCategoryEnum.OBST
        val expectedFoodList = listOf(FoodLocal(name = "Apple", category = category))

        `when`(foodDao.getFoodByCategory(category)).thenReturn(expectedFoodList)

        val result = foodController.getFoodByCategory(category)
        assertEquals(expectedFoodList, result)
    }

    @Test
    fun `test searchFoodByName returns empty list for invalid name`() = runBlocking {
        val invalidName = "NonExistentFood"

        `when`(foodDao.getFoodByName(invalidName)).thenReturn(emptyList())

        val result = foodController.searchFoodByName(invalidName)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getAllFood returns empty list when no food items exist`() = runBlocking {
        `when`(foodDao.getAllFood()).thenReturn(flowOf(emptyList()))

        val result = foodController.getAllFood()
        val collectedResult = result.first()  // Collect the Flow value
        assertTrue(collectedResult.isEmpty())
    }

    @Test
    fun `test getFoodByCategory returns empty list for invalid category`() = runBlocking {
        val invalidCategory = FoodCategoryEnum.UNBEKANNT

        `when`(foodDao.getFoodByCategory(invalidCategory)).thenReturn(emptyList())

        val result = foodController.getFoodByCategory(invalidCategory)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getAllFood handles large number of food items`() = runBlocking {
        val largeFoodList = List(1000) { index ->
            FoodLocal(name = "Food$index", category = FoodCategoryEnum.OBST)
        }

        `when`(foodDao.getAllFood()).thenReturn(flowOf(largeFoodList))

        val result = foodController.getAllFood().first()  // Collect the Flow value
        assertEquals(largeFoodList, result)
    }
}
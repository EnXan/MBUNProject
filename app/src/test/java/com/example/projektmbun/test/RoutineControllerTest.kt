package com.example.projektmbun.controller

import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.routine.Routine
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.local.daos.RoutineDao
import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class RoutineControllerTest {

    private lateinit var routineDao: RoutineDao
    private lateinit var foodCardDao: FoodCardDao
    private lateinit var routineController: RoutineController

    @Before
    fun setUp() {
        routineDao = mock(RoutineDao::class.java)
        foodCardDao = mock(FoodCardDao::class.java)
        routineController = RoutineController(routineDao, foodCardDao)
    }

    @Test
    fun `test add or update routine`(): Unit = runBlocking {
        // Arrange
        val routine = Routine(id = 1, name = "Test Routine", startDate = "01.01.2023", rhythm = "Täglich", isActive = true)
        `when`(routineDao.upsertRoutine(routine)).thenReturn(1L)

        // Act
        routineController.addOrUpdateRoutine(routine)

        // Assert
        verify(routineDao, times(1)).upsertRoutine(routine)
    }

    @Test
    fun `test update routine is active`(): Unit = runBlocking {
        // Arrange
        val routineId = 1
        `when`(routineDao.updateIsActive(routineId, false)).thenReturn(1)

        // Act
        routineController.updateRoutineIsActive(routineId, false)

        // Assert
        verify(routineDao, times(1)).updateIsActive(routineId, false)
    }

    @Test
    fun `test delete routine`(): Unit = runBlocking {
        // Arrange
        val routineId = 1
        `when`(routineDao.deleteRoutineById(routineId)).thenReturn(1)

        // Act
        routineController.deleteRoutine(routineId)

        // Assert
        verify(routineDao, times(1)).deleteRoutineById(routineId)
    }

    @Test
    fun `test get all active routines`(): Unit = runBlocking {
        // Arrange
        val testRoutines = listOf(
            Routine(id = 1, name = "Test Routine 1", startDate = "01.01.2023", rhythm = "Täglich", isActive = true),
            Routine(id = 2, name = "Test Routine 2", startDate = "02.01.2023", rhythm = "Wöchentlich", isActive = true)
        )
        `when`(routineDao.getAllActiveRoutines()).thenReturn(testRoutines)

        // Act
        val activeRoutines = routineController.getAllActiveRoutines()

        // Assert
        assertNotNull(activeRoutines)
        assertEquals(testRoutines, activeRoutines)
        verify(routineDao, times(1)).getAllActiveRoutines()
    }

    @Test
    fun `test execute routine if due today`(): Unit = runBlocking {
        // Arrange
        val routine = Routine(id = 1, name = "Test Routine", startDate = "01.01.2023", rhythm = "Täglich", isActive = true)

        // Mock für getFoodCardsByRoutineId
        val testFoodCards = listOf(
            FoodCard(
                id = 1,
                routineId = 1,
                stockId = 1,
                foodId = "Apfel Test",
                quantity = 1.0,
                unit = UnitsEnum.STUECK,
                expiryDate = "2024-12-31",
                isActive = true
            ),
            FoodCard(
                id = 2,
                routineId = 1,
                stockId = 1,
                foodId = "Banane Test",
                quantity = 2.0,
                unit = UnitsEnum.KILOGRAMM,
                expiryDate = "2024-12-31",
                isActive = true
            )
        )
        `when`(foodCardDao.getFoodCardsByRoutineId(routine.id!!)).thenReturn(testFoodCards)

        // Mock für updateFoodCardStockIdByFoodCardId
        `when`(foodCardDao.updateFoodCardStockIdByFoodCardId(anyInt(), anyInt())).thenReturn(1)

        // Act
        routineController.executeRoutineIfDueToday(routine)

        // Assert
        verify(foodCardDao).getFoodCardsByRoutineId(routine.id!!)
        verify(foodCardDao, times(testFoodCards.size))
            .updateFoodCardStockIdByFoodCardId(anyInt(), anyInt())
    }

    // Optional: Aufräumen nach den Tests
    @After
    fun tearDown() {
        // Wenn Sie Mockito-Mocks aufräumen möchten
        clearAllCaches()
    }
}
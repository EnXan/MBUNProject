package com.example.projektmbun.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.models.data_structure.routine.Routine
import com.example.projektmbun.models.database.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class RoutineIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var routineController: RoutineController

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        routineController = RoutineController(db.routineDao(), db.foodCardDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAddAndRetrieveRoutine() = runBlocking {
        val routine = Routine(id = 1, name = "Test Routine", startDate = "01.01.2023", rhythm = "Täglich", isActive = true)
        routineController.addOrUpdateRoutine(routine)

        val allRoutines = routineController.getAllRoutines()
        assertTrue("Routine should be added", allRoutines.contains(routine))
    }

    @Test
    fun testUpdateRoutineIsActive() = runBlocking {
        val routine = Routine(id = 1, name = "Test Routine", startDate = "01.01.2023", rhythm = "Täglich", isActive = true)
        routineController.addOrUpdateRoutine(routine)

        routineController.updateRoutineIsActive(routine.id!!, false)
        val updatedRoutine = routineController.getAllRoutines().find { it.id == routine.id }
        assertNotNull("Updated routine should not be null", updatedRoutine)
        assertFalse("Routine should be inactive", updatedRoutine!!.isActive)
    }

    @Test
    fun testDeleteRoutine() = runBlocking {
        val routine = Routine(id = 1, name = "Test Routine", startDate = "01.01.2023", rhythm = "Täglich", isActive = true)
        routineController.addOrUpdateRoutine(routine)

        routineController.deleteRoutine(routine.id!!)
        val allRoutines = routineController.getAllRoutines()
        assertFalse("Routine should be deleted", allRoutines.contains(routine))
    }

    @Test
    fun testExecuteRoutineIfDueToday() = runBlocking {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val today = LocalDate.now().format(formatter)

        val routine = Routine(id = 1, name = "Test Routine", startDate = today, rhythm = "Täglich", isActive = true)
        routineController.addOrUpdateRoutine(routine)

        routineController.executeRoutineIfDueToday(routine)

        // Hole die aktualisierte Routine aus der Datenbank
        val updatedRoutine = routineController.getAllRoutines().find { it.id == routine.id }

        assertNotNull("Updated routine should not be null", updatedRoutine)
        assertTrue("Routine should still be active", updatedRoutine!!.isActive)
        assertNotNull("Last execution date should not be null", updatedRoutine.lastExecutionDate)
        System.out.println("${updatedRoutine.lastExecutionDate}")
        assertEquals("Last execution date should be today", today, updatedRoutine.lastExecutionDate)
    }
}
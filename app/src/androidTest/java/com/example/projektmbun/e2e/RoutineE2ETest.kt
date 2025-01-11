package com.example.projektmbun.e2e

import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import com.example.projektmbun.R
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.views.activities.MainActivity
import org.junit.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.projektmbun.models.data_structure.routine.Routine
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class RoutineE2ETest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var routineController: RoutineController

    @Before
    fun setup() {
        // Use the activity context to access the database
        activityRule.scenario.onActivity { activity ->
            // Disable animations via window manager
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            val db = AppDatabase.getDatabase(activity.applicationContext)
            routineController = RoutineController(db.routineDao(), db.foodCardDao())
        }
    }

    @Test
    fun testRoutineExecution()  {
        runBlocking {
        // 1. Create a new routine
        val routine = Routine(
            id = null,
            name = "Test Routine",
            startDate = "01.01.2023",
            rhythm = "Täglich",
            isActive = true
        )
        routineController.addOrUpdateRoutine(routine)

        // Wait for database operation to complete
        delay(500)

        // 2. Verify routine is added and get the latest version with ID
        val savedRoutine = routineController.getAllRoutines()
            .find { it.name == "Test Routine" }
        assertTrue("Routine should be added", savedRoutine != null)

        // 3. Execute routine if due today
        savedRoutine?.let { routineController.executeRoutineIfDueToday(it) }

        // 4. Verify routine execution
        val updatedRoutine = routineController.getAllRoutines()
            .find { it.name == "Test Routine" }
        assertNotNull("Updated routine should not be null", updatedRoutine)
        assertEquals(
            "Last execution date should be today",
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            updatedRoutine?.lastExecutionDate
        )

        // 5. Verify UI updates - First check RecyclerView is displayed
        onView(withId(R.id.routine_recycler_view))
            .check(matches(isDisplayed()))

        // Verify RecyclerView is populated
        onView(withId(R.id.routine_recycler_view))
            .check(matches(hasMinimumChildCount(1)))
    }
    }
}
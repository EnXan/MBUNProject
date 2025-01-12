package com.example.projektmbun.e2e

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.views.activities.MainActivity
import com.example.projektmbun.views.adapters.FoodAdapter
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.*
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withId
import kotlinx.coroutines.delay

@RunWith(AndroidJUnit4::class)
class FoodE2ETest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById<View>(id)
                v.performClick()
            }
        }
    }

    private fun waitFor(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription(): String = "Wait for $millis milliseconds"
            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }

    @Test
    fun testCompleteAddFoodProcess() = runBlocking {
        lateinit var foodCardController: FoodCardController

        // Setup: Controller-Referenzen aus der Activity holen
        activityRule.scenario.onActivity { activity ->
            val db = AppDatabase.getDatabase(activity.applicationContext)
            foodCardController = FoodCardController(db.foodCardDao(), FoodService())
        }

        // Jetzt den Test ausführen mit dem Controller aus der Activity
        val initialCount = foodCardController.getFoodCardsInStock().size

        onView(allOf(withId(R.id.fragment_stock), isDisplayed()))
            .perform(click())

        onView(withId(R.id.food_recycler_view))
            .check(matches(isDisplayed()))

        onView(withId(R.id.food_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FoodAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.btn_select_quantity)
                )
            )

        onView(allOf(withText("OK"), isDisplayed()))
            .inRoot(isDialog())
            .perform(click())

        onView(withId(R.id.food_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FoodAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.btn_add_to_stock)
                )
            )

        // Warte bis Operation abgeschlossen ist
        onView(isRoot()).perform(waitFor(1000))

        // Hole finalen Count
        val finalCount = foodCardController.getFoodCardsInStock().size

        // Final assertion
        assertEquals("Should have one more item in stock", initialCount + 1, finalCount)
    }

    @Test
    fun testAddDuplicateFoodEntries(): Unit = runBlocking {
        lateinit var foodCardController: FoodCardController

        // Setup: Controller-Referenzen aus der Activity holen
        activityRule.scenario.onActivity { activity ->
            val db = AppDatabase.getDatabase(activity.applicationContext)
            foodCardController = FoodCardController(db.foodCardDao(), FoodService())
        }

        val initialCards = foodCardController.getFoodCardsInStock()

        // Setup: Add a food item
        onView(allOf(withId(R.id.fragment_stock), isDisplayed()))
            .perform(click())

        onView(withId(R.id.food_recycler_view))
            .check(matches(isDisplayed()))

        onView(withId(R.id.food_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FoodAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.btn_select_quantity)
                )
            )

        onView(allOf(withText("OK"), isDisplayed()))
            .inRoot(isDialog())
            .perform(click())

        onView(withId(R.id.food_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FoodAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.btn_add_to_stock)
                )
            )

        // Attempt to add the same food item again
        onView(allOf(withId(R.id.fragment_stock), isDisplayed()))
            .perform(click())

        onView(withId(R.id.food_recycler_view))
            .check(matches(isDisplayed()))

        onView(withId(R.id.food_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FoodAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.btn_select_quantity)
                )
            )

        onView(allOf(withText("OK"), isDisplayed()))
            .inRoot(isDialog())
            .perform(click())

        onView(withId(R.id.food_recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FoodAdapter.ViewHolder>(
                    0,
                    clickChildViewWithId(R.id.btn_add_to_stock)
                )
            )

        delay(1000) // Sicherstellen, dass DB-Operationen abgeschlossen sind
        val finalCards = foodCardController.getFoodCardsInStock()


        // Final assertion
        assertEquals("Should have one more item in stock", initialCards.size + 2, finalCards.size)
    }
}
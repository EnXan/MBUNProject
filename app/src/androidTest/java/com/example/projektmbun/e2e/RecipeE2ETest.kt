package com.example.projektmbun.e2e

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.projektmbun.R
import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.views.activities.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RecipeE2ETest {

    private lateinit var recipeController: RecipeController
    private val recipeService = RecipeService()

    @Rule
    @JvmField
    var mRuntimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.READ_MEDIA_IMAGES)

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)




    @Before
    fun setUp() {
        Intents.init()

        // Use the activity context to access the database
        activityRule.scenario.onActivity {
                        recipeController = RecipeController(recipeService)
        }
    }

    @After
    fun tearDown() {   // Kein suspend mehr
        runBlocking {  // Startet Coroutine innerhalb der Methode
            recipeController.removeRecipeByTitle("Test Rezept")
        }
        Intents.release()
    }

    private fun setProgress(progress: Int): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController?, view: View) {
                val seekBar = view as SeekBar
                seekBar.progress = progress
            }

            override fun getDescription(): String {
                return "Set a progress on a SeekBar"
            }

            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isAssignableFrom(SeekBar::class.java)
            }
        }
    }

    @Test
    fun testCompleteAddRecipeProcess(): Unit = runBlocking {

        onView(allOf(withId(R.id.fragment_recipes)))
            .perform(click())

        onView(withId(R.id.fragment_recipes))
            .check(matches(isDisplayed()))

        // Add a delay to wait for fragment transition
        delay(500)

        onView(withId(R.id.add_recipe))
            .perform(click())

        val intent = Intent()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = Uri.parse("android.resource://${context.packageName}/drawable/test_image")
        intent.data = uri

        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, intent))

        onView(withId(R.id.recipe_image_uploader))
            .perform(click())


        delay(1000)


        // For form fields, keep scrollTo() since they are likely in a scrollable container
        onView(allOf(withId(R.id.recipe_title)))
            .perform(ViewActions.scrollTo(), replaceText("Test Rezept"))

        onView(allOf(withId(R.id.button_plus)))
            .perform(ViewActions.scrollTo(), click())

        onView(withId(R.id.type_choose))
            .perform(ViewActions.scrollTo(), click())
        onData(anything())
            .atPosition(2)
            .perform(click())

        onView(allOf(withId(R.id.edit_recipe_desc_multiline)))
            .perform(ViewActions.scrollTo(), replaceText("Lorem ipsum dolor sit amet..."))

        onView(withId(R.id.preptime_seekbar))
            .perform(ViewActions.scrollTo(), setProgress(10))

        onView(withId(R.id.cooktime_seekbar))
            .perform(ViewActions.scrollTo(), setProgress(15))


        onView(allOf(withId(R.id.recipe_src_text)))
            .perform(ViewActions.scrollTo(), replaceText("Test Recipe Source"))

        onView(allOf(withId(R.id.option_fortgeschritten)))
            .perform(ViewActions.scrollTo(), click())

        onView(allOf(withId(R.id.checkbox_glutenfrei)))
            .perform(ViewActions.scrollTo(), click())

        onView(allOf(withId(R.id.checkbox_vegetarisch)))
            .perform(ViewActions.scrollTo(), click())

        onView(allOf(withId(R.id.ingredient_name_text)))
            .perform(ViewActions.scrollTo(), replaceText("Test Ingredient"))

        onView(withId(R.id.category_chooser))
            .perform(ViewActions.scrollTo(), click())
        onData(anything())
            .atPosition(2)
            .perform(click())

        onView(allOf(withId(R.id.ingredient_desc_text_multiline)))
            .perform(ViewActions.scrollTo(), replaceText("Test Beschreibung"))

        onView(allOf(withId(R.id.ingredient_amount_text)))
            .perform(ViewActions.scrollTo(), replaceText("500"))

        onView(withId(R.id.unit_chooser))
            .perform(ViewActions.scrollTo(), click())
        onData(anything())
            .atPosition(1)
            .perform(click())

        onView(allOf(withId(R.id.ingredient_price_text)))
            .perform(ViewActions.scrollTo(), replaceText("1.0"))

        onView(allOf(withId(R.id.add_ingredients_to_recipe)))
            .perform(ViewActions.scrollTo(), click())

        onView(allOf(withId(R.id.edit_text_multiline_instructions)))
            .perform(ViewActions.scrollTo(), replaceText("Test Instruction 1"))

        onView(allOf(withId(R.id.add_instructions_to_recipe)))
            .perform(ViewActions.scrollTo(), click())

        onView(allOf(withId(R.id.edit_text_multiline_instructions)))
            .perform(ViewActions.scrollTo(), replaceText("Test Instruction 2"))

        onView(allOf(withId(R.id.add_instructions_to_recipe)))
            .perform(ViewActions.scrollTo(), click())

        // Nach dem Submit Button Click
        onView(allOf(withId(R.id.submit_recipe)))
            .perform(ViewActions.scrollTo(), click())

        delay(1000)

        val result = recipeController.getRecipesByTitle("Test Rezept")

        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertEquals("Test Rezept", result.first().title)


    }
}

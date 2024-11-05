package com.example.projektmbun.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.fragment.app.Fragment
import com.example.projektmbun.R
import com.example.projektmbun.views.fragments.StockFragment
import com.example.projektmbun.views.fragments.MenuFragment
import com.example.projektmbun.views.fragments.RecipesFragment
import com.example.projektmbun.views.fragments.RoutinesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import java.util.concurrent.Executors

/**
 * MainActivity is the main entry point of the app, providing a bottom navigation
 * view for navigating between fragments and initializing the EmojiCompat library.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Called when the activity is starting. This is where most initialization should happen.
     * The method initializes EmojiCompat for emoji support, sets up the bottom navigation
     * listener, and loads the default fragment.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize EmojiCompat for emoji support
        val executor = Executors.newSingleThreadExecutor()
        val config = BundledEmojiCompatConfig(this, executor)
        EmojiCompat.init(config)

        setContentView(R.layout.activity_main)

        // Set up bottom navigation view and its item selection listener
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener(navListener)

        // Load the default fragment if this is the first time the activity is created
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MenuFragment()).commit()
        }
    }

    /**
     * Listener for bottom navigation item selection. Replaces the current fragment with
     * the selected fragment based on the item ID.
     */
    private val navListener = NavigationBarView.OnItemSelectedListener { item ->
        val selectedFragment: Fragment = when (item.itemId) {
            R.id.nav_home -> MenuFragment()
            R.id.nav_stock -> StockFragment()
            R.id.nav_routines -> RoutinesFragment()
            R.id.nav_recipes -> RecipesFragment()
            else -> StockFragment()
        }

        // Replace the fragment container with the selected fragment
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
        true
    }
}

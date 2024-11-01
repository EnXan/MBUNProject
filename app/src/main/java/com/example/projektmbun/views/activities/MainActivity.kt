package com.example.projektmbun.views.activities

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.projektmbun.R
import com.example.projektmbun.views.fragments.FridgeFragment
import com.example.projektmbun.views.fragments.RecipesFragment
import com.example.projektmbun.views.fragments.RoutinesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(navListener)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FridgeFragment()).commit()
        }
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val selectedFragment: Fragment = when (item.itemId) {
            R.id.nav_fridge -> FridgeFragment()
            R.id.nav_routines -> RoutinesFragment()
            R.id.nav_recipes -> RecipesFragment()
            else -> FridgeFragment()
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
        true
    }

}
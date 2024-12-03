package com.example.projektmbun.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.projektmbun.R
import com.example.projektmbun.databinding.ActivityMainBinding
import com.example.projektmbun.workers.RoutineExecutionWorker
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize EmojiCompat for emoji support
        val executor = Executors.newSingleThreadExecutor()
        val config = BundledEmojiCompatConfig(this, executor)
        EmojiCompat.init(config)

        // Set up BottomNavigationView with NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        // Initialize and set up the routine worker
        setupRoutineWorker()

    }

    private fun setupRoutineWorker() {
        // Define constraints
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // Only run when battery is not low
            .setRequiresCharging(true)     // Only run when device is charging
            .build()

        // Create a periodic work request
        val routineWorkerRequest = PeriodicWorkRequestBuilder<RoutineExecutionWorker>(
            1, TimeUnit.DAYS // Run daily
        )
            .setConstraints(constraints) // Add constraints
            .setInitialDelay(24, TimeUnit.HOURS) // Wait 24 hours before the first run
            .build()

        // Enqueue the work request
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RoutineExecution", // Unique name for this worker
            ExistingPeriodicWorkPolicy.UPDATE, // Replace existing work with the same name
            routineWorkerRequest
        )
    }
}

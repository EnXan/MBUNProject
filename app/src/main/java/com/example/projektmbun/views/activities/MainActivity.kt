package com.example.projektmbun.views.activities

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.projektmbun.R
import com.example.projektmbun.databinding.ActivityMainBinding
import com.example.projektmbun.models.cloud.listener.FoodListener
import com.example.projektmbun.utils.NetworkMonitor
import com.example.projektmbun.workers.RoutineExecutionWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NetworkMonitor.NetworkStatusListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var networkMonitor: NetworkMonitor
    private var wasOffline = false

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
        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.itemActiveIndicatorColor = ContextCompat.getColorStateList(this, R.color.primary_green)
        bottomNavigationView.setupWithNavController(navController)

        // Initialize network monitor
        networkMonitor = NetworkMonitor(this)

        // Set initial offline state based on current connectivity
        wasOffline = !isInternetAvailable()

        // Initialize and set up the routine worker
        setupRoutineWorker()

        // If internet is available at startup, initialize the app
        if (isInternetAvailable()) {
            FoodListener(this).initializeApp(CoroutineScope(Dispatchers.IO))
        }
    }

    override fun onResume() {
        super.onResume()
        // Register network monitor when activity comes to foreground
        registerReceiver(
            networkMonitor,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onPause() {
        super.onPause()
        // Unregister network monitor when activity goes to background
        try {
            unregisterReceiver(networkMonitor)
        } catch (e: IllegalArgumentException) {
            // Receiver wasn't registered
        }
    }

    override fun onNetworkStatusChanged(isConnected: Boolean) {
        if (isConnected && wasOffline) {
            // Only initialize if we were previously offline
            FoodListener(this).initializeApp(CoroutineScope(Dispatchers.IO))
            wasOffline = false
        } else if (!isConnected) {
            wasOffline = true
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupRoutineWorker() {
        // Define constraints
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .build()

        // Create a periodic work request
        val routineWorkerRequest = PeriodicWorkRequestBuilder<RoutineExecutionWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        // Enqueue the work request
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RoutineExecution",
            ExistingPeriodicWorkPolicy.UPDATE,
            routineWorkerRequest
        )
    }
}
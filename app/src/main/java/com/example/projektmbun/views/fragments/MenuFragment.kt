package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.databinding.FragmentMenuBinding
import com.example.projektmbun.models.daos.FoodCardDao
import com.example.projektmbun.models.daos.FoodCardWithDetailsDao
import com.example.projektmbun.models.daos.RoutineDao
import com.example.projektmbun.models.data.relations.FoodCardWithDetails
import com.example.projektmbun.models.data.routine.Routine
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.SpaceItemDecoration
import com.example.projektmbun.utils.enums.FoodCardStateEnum
import com.example.projektmbun.views.adapters.ExpiryDateMenuAdapter
import com.example.projektmbun.views.adapters.FoodCardMenuAdapter
import com.example.projektmbun.views.adapters.RoutineMenuAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodCardDao: FoodCardDao
    private lateinit var foodCardController: FoodCardController
    private lateinit var foodCardWithDetailsDao: FoodCardWithDetailsDao
    private lateinit var foodCardMenuAdapter: FoodCardMenuAdapter
    private lateinit var expiryDateMenuAdapter: ExpiryDateMenuAdapter
    private lateinit var routineMenuAdapter: RoutineMenuAdapter
    private lateinit var routineDao: RoutineDao
    private lateinit var routineController: RoutineController
    private lateinit var addFoodButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        // Layout für dieses Fragment festlegen
        val view = binding.root
        foodCardDao = AppDatabase.getDatabase(requireContext()).foodCardDao()
        foodCardWithDetailsDao = AppDatabase.getDatabase(requireContext()).foodCardWithDetailsDao()
        foodCardController = FoodCardController(foodCardDao, foodCardWithDetailsDao)
        routineDao = AppDatabase.getDatabase(requireContext()).routineDao()
        routineController = RoutineController(routineDao, foodCardDao)

        foodCardMenuAdapter = FoodCardMenuAdapter(emptyList())
        expiryDateMenuAdapter = ExpiryDateMenuAdapter(emptyList())
        routineMenuAdapter = RoutineMenuAdapter(emptyList())


        // RecyclerView 1: FoodCards
        val foodCardRecyclerView = binding.stockRecyclerView
        foodCardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        foodCardRecyclerView.adapter = foodCardMenuAdapter

        // RecyclerView 2: ExpiryDates
        val expiryDateRecyclerView = binding.expiryDateRecyclerView
        expiryDateRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        expiryDateRecyclerView.adapter = expiryDateMenuAdapter

        // RecyclerView 3: Routines
        val routineRecyclerView = binding.routineRecyclerView
        routineRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        routineRecyclerView.adapter = routineMenuAdapter

        loadFoodCards()

        //Set space between every food element
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        val itemDecoration = SpaceItemDecoration(spacing)
        foodCardRecyclerView.addItemDecoration(itemDecoration)
        expiryDateRecyclerView.addItemDecoration(itemDecoration)
        routineRecyclerView.addItemDecoration(itemDecoration)

        addFoodButton = binding.lebensmittelAddButton
        addFoodButton.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_menu_fragment_to_stock_fragment,
                null,
                navOptions {
                    popUpTo(R.id.fragment_menu) { inclusive = true }
                }
            )
        }
        return view
    }

    private fun loadFoodCards() {
        // Launch a coroutine on the IO dispatcher to handle database operations
        lifecycleScope.launch(Dispatchers.IO) {
            var foodCardWithDetails: List<FoodCardWithDetails> = emptyList()

            try {
                // Lade alle FoodCards mit Details aus der Datenbank
                foodCardWithDetails = foodCardController.getFoodCardsInStock()
            } catch (e: Exception) {
                Log.e("MenuFragment", "Error loading food cards", e)
            }

            var routines: List<Routine> = emptyList()

            try {
                routines = routineController.getAllActiveRoutines()
            } catch (e: Exception) {
                Log.e("MenuFragment", "Error loading routines", e)
            }

            // Filtere nur die FoodCards mit einem nicht-null Haltbarkeitsdatum für den expiryDateMenuAdapter
            val foodCardsWithExpiryDate = foodCardWithDetails.filter {
                it.foodCard.expiryDate != null
            }.sortedBy {
                // Sortiere nach dem Datum (ältestes Datum zuerst)
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                sdf.parse(it.foodCard.expiryDate)?.time ?: Long.MAX_VALUE
            }

            // Switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
                // Alle FoodCards an foodCardMenuAdapter weitergeben
                foodCardMenuAdapter.updateData(foodCardWithDetails)
                // Gefilterte und sortierte FoodCards an expiryDateMenuAdapter weitergeben
                expiryDateMenuAdapter.updateData(foodCardsWithExpiryDate)
                // Alle aktiven Routines
                routineMenuAdapter.updateData(routines)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        loadFoodCards()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
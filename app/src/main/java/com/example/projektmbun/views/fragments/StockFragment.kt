package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.controller.FoodController
import com.example.projektmbun.controller.StockController
import com.example.projektmbun.databinding.FragmentStockBinding
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.local.daos.StockDao
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.local.daos.FoodDao
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.SpaceItemDecoration
import com.example.projektmbun.utils.addSearchListener
import com.example.projektmbun.views.adapters.FoodAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Filter(val name: ImageButton, val categoryTextView: TextView, var isActive: Boolean) {
    fun toggleActive(active: Boolean) {
        isActive = active
        name.isSelected = active
    }
}

class StockFragment : Fragment() {



    private var routineId: Int? = null
    private var showFinishButton: Boolean = false

    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var stockDao: StockDao
    private lateinit var stockController: StockController
    private lateinit var foodCardDao: FoodCardDao
    private lateinit var foodDao: FoodDao
    private lateinit var foodCardController: FoodCardController
    private lateinit var foodController: FoodController

    val STOCK_ID: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = StockFragmentArgs.fromBundle(requireArguments())
        routineId = args.routineId.takeIf { it != -1 }
        showFinishButton = args.showFinishButton
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        val view = binding.root
        val foodService = FoodService()
        foodDao = AppDatabase.getDatabase(requireContext()).foodDao()
        foodController = FoodController(foodService, foodDao)

        foodCardDao = AppDatabase.getDatabase(requireContext()).foodCardDao()
        foodCardController = FoodCardController(foodCardDao, foodService)

        stockDao = AppDatabase.getDatabase(requireContext()).stockDao()
        stockController = StockController(stockDao, foodCardDao)


        foodAdapter = FoodAdapter(emptyList(), requireContext(), showFinishButton, routineId, STOCK_ID)
        binding.foodRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
            addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.item_spacing)))
        }

        if (showFinishButton) {
            view.findViewById<ConstraintLayout>(R.id.finish_button).visibility = View.VISIBLE

            binding.finishButton.setOnClickListener {
                it.findNavController().navigate(R.id.action_fragment_stock_to_fragment_routine)
            }
        }

        // Verwende NavController für die Navigation zu StockListFragment
        binding.btnFoodCounter.setOnClickListener {
            it.findNavController().navigate(R.id.action_stock_fragment_to_stock_list_fragment)
        }

        binding.searchBar.searchEditText.addSearchListener(lifecycleScope) { query ->
            lifecycleScope.launch(Dispatchers.IO) {
                val results = foodController.searchFoodByName(query)
                withContext(Dispatchers.Main) {
                    updateRecyclerView(results)
                }
            }
        }

        val filters = listOf(
            Filter(binding.gemueseFilter, binding.gemueseFilterLabel, false),
            Filter(binding.obstFilter, binding.obstFilterLabel, false),
            Filter(binding.fleischFilter, binding.fleischFilterLabel, false),
            Filter(binding.getreideFilter, binding.getreideFilterLabel, false)
        )

        lifecycleScope.launch(Dispatchers.IO) {
            fetchFoodWithFilters(filters)
        }

        observeFoodCounter()

        filters.forEach { filter ->
            filter.name.setOnClickListener {
                if (filter.isActive) {
                    filter.toggleActive(false)
                    if (filters.none { it.isActive }) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            fetchFoodWithFilters(filters)
                        }
                    }
                } else {
                    filters.forEach { it.toggleActive(false) }
                    filter.toggleActive(true)
                    lifecycleScope.launch(Dispatchers.IO) {
                        fetchFoodWithFilters(filters)
                    }
                }
            }
        }
        observeFoodFlow()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateRecyclerView(foodLocalList: List<FoodLocal>) {
        foodAdapter.updateData(foodLocalList)
    }

    private fun observeFoodCounter() {
        lifecycleScope.launch {
            try {
                foodCardDao.getFoodCardCountFlow()
                    .distinctUntilChanged()
                    .collect { count ->
                        withContext(Dispatchers.Main) {
                            // Überprüfe, ob die View noch existiert
                            binding?.btnFoodCounter?.text = count.toString()
                        }
                    }
            } catch (e: Exception) {
                Log.e("StockFragment", "Error observing food counter", e)
            }
        }
    }



    private fun observeFoodFlow() {
        lifecycleScope.launch {
            foodDao.getAllFood().distinctUntilChanged()
                .collect { foodList ->
                    foodAdapter.updateData(foodList)
                }
        }
    }

    private suspend fun fetchFoodWithFilters(filters: List<Filter>) {
        val activeFilters = filters.filter { it.isActive }
            .mapNotNull { Converters.toCategoryEnum(it.categoryTextView.text.toString()) }

        val dataset = if (activeFilters.isEmpty()) {
            // Sammle den Flow von `getAllFood`
            val result = foodController.getAllFood().first() // Aktuelle Liste abrufen
            Log.d("CreateRecipeFragment", "Ergebnis von getAllFood: $result") // Ergebnis loggen
            result
        } else {
            val categoryEnum = activeFilters.firstOrNull()
            if (categoryEnum != null) {
                foodController.getFoodByCategory(categoryEnum) // Direktes Ergebnis, kein Flow
            } else {
                emptyList()
            }
        }

        withContext(Dispatchers.Main) {
            foodAdapter.updateData(dataset)
        }
    }

}

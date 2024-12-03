package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.projektmbun.models.daos.FoodCardDao
import com.example.projektmbun.models.daos.FoodCardWithDetailsDao
import com.example.projektmbun.models.daos.StockDao
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.models.data.stock.Stock
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.SpaceItemDecoration
import com.example.projektmbun.utils.addSearchListener
import com.example.projektmbun.views.adapters.FoodAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Filter(val name: Button, val categoryTextView: TextView, var isActive: Boolean) {
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
    private lateinit var foodCardController: FoodCardController
    private lateinit var foodController: FoodController
    private lateinit var foodCardWithDetailsDao: FoodCardWithDetailsDao

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
        val foodDao = AppDatabase.getDatabase(requireContext()).foodDao()
        foodController = FoodController(foodDao)

        foodCardWithDetailsDao = AppDatabase.getDatabase(requireContext()).foodCardWithDetailsDao()

        foodCardDao = AppDatabase.getDatabase(requireContext()).foodCardDao()
        foodCardController = FoodCardController(foodCardDao, foodCardWithDetailsDao)

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
                val results = foodController.getFoodByName(query)
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

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateRecyclerView(foodList: List<Food>) {
        foodAdapter.updateData(foodList)
    }

    private suspend fun fetchFoodWithFilters(filters: List<Filter>) {
        val activeFilters = filters.filter { it.isActive }
            .mapNotNull { Converters.getCategoryEnumFromText(it.categoryTextView.text.toString()) }
        val dataset = if (activeFilters.isEmpty()) {
            foodController.getAllFood()
        } else {
            val categoryEnum = activeFilters.firstOrNull()
            if (categoryEnum != null) {
                foodController.getFoodByCategory(categoryEnum)
            } else {
                emptyList()
            }
        }

        withContext(Dispatchers.Main) {
            foodAdapter.updateData(dataset)
        }
    }
}

package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.databinding.FragmentStockListBinding
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.local.daos.StockDao
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.SpaceItemDecoration
import com.example.projektmbun.utils.addSearchListener
import com.example.projektmbun.views.adapters.StockFoodCardListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StockListFragment : Fragment() {

    private var _binding: FragmentStockListBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodCardDao: FoodCardDao
    private lateinit var foodCardController: FoodCardController
    private lateinit var stockFoodCardListAdapter: StockFoodCardListAdapter
    private lateinit var closeButton: LinearLayout
    private lateinit var stockDao: StockDao
    private lateinit var foodCounterButton: Button
    private lateinit var foodService: FoodService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStockListBinding.inflate(inflater, container, false)
        val view = binding.root
        foodCardDao = AppDatabase.getDatabase(requireContext()).foodCardDao()
        foodService = FoodService()
        foodCardController = FoodCardController(foodCardDao, foodService)

        stockDao = AppDatabase.getDatabase(requireContext()).stockDao()

        binding.searchEditText.searchEditText.hint = "Lebensmittel im Vorrat suchen..."

        binding.searchEditText.scanIcon.setImageDrawable(null)

        binding.searchEditText.searchEditText.addSearchListener(lifecycleScope) { query ->
            lifecycleScope.launch(Dispatchers.IO) {
                // Ergebnisse der Suche abrufen
                val results = foodCardController.getFoodCardsByName(query)

                // RecyclerView mit den Ergebnissen aktualisieren
                withContext(Dispatchers.Main) {
                    updateRecyclerView(results)
                }
            }
        }

        observeFoodCounter()

        closeButton = binding.btnCloseStocklist
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        foodCounterButton = binding.btnFoodCounter

        val recyclerView: RecyclerView = binding.foodlistStockView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        stockFoodCardListAdapter = StockFoodCardListAdapter(emptyList(), requireContext())
        recyclerView.adapter = stockFoodCardListAdapter

        loadFoodCards()

        //Set space between every food element
        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        val itemDecoration = SpaceItemDecoration(spacing)
        recyclerView.addItemDecoration(itemDecoration)
        return view
    }

    private fun loadFoodCards() {
        lifecycleScope.launch(Dispatchers.IO) {
            val stockFoodCardWithFoodList = foodCardController.getFoodCardsInStock()
            withContext(Dispatchers.Main) {
                if (stockFoodCardWithFoodList.isEmpty()) {
                    binding.stocklistErrorText.apply {
                        text = "Keine Lebensmittel im Vorrat"
                        visibility = View.VISIBLE
                    }
                    binding.foodlistStockView.visibility = View.GONE
                } else {
                    binding.stocklistErrorText.visibility = View.GONE
                    binding.foodlistStockView.visibility = View.VISIBLE
                    stockFoodCardListAdapter.updateData(stockFoodCardWithFoodList)
                }
            }
        }
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



    override fun onResume() {
        super.onResume()
        loadFoodCards()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateRecyclerView(foodCardList: List<FoodCardWithDetails>) {
        stockFoodCardListAdapter.updateData(foodCardList)
    }
}
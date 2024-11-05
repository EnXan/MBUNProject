package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodController
import com.example.projektmbun.models.data.Food
import com.example.projektmbun.models.database.FoodDatabase
import com.example.projektmbun.utils.SpaceItemDecoration
import com.example.projektmbun.views.adapters.FoodAdapter
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Fragment that displays a list of food items.
 * It supports filtering by food category and searching by food name.
 */
class StockFragment : Fragment() {

    private lateinit var foodController: FoodController
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var foodMap: Map<String, Food>
    private lateinit var searchBar: EditText
    private lateinit var filterGemuese: Button
    private lateinit var filterObst: Button
    private lateinit var filterFleisch: Button
    private lateinit var filterGetreide: Button
    private lateinit var recyclerView: RecyclerView
    private var selectedButton: Button? = null

    private var currentQuery: String = ""
    private var currentCategory: String? = null

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater LayoutInflater object that can be used to inflate views.
     * @param container Parent view that this fragment's UI should be attached to.
     * @param savedInstanceState Previous state of the fragment, if available.
     * @return The root View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stock, container, false)

        try {
            // Initialize the FoodController
            val foodDao = FoodDatabase.getDatabase(requireContext()).foodDao()
            foodController = FoodController(foodDao)
        } catch (e: Exception) {
            Log.e("StockFragment", "Error initializing FoodController", e)
            return null
        }

        // Set up RecyclerView with a LinearLayoutManager and adapter.
        recyclerView = view.findViewById(R.id.food_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        foodAdapter = FoodAdapter(emptyList(), requireContext()) // Initialize with an empty list
        recyclerView.adapter = foodAdapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels))

        // Load `Food` data into the adapter
        loadFoodData()

        // Initialize Search Bar and set up a listener for text changes.
        searchBar = view.findViewById(R.id.search_edit_text)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString() ?: ""
                applyFilters()
            }
        })

        // Initialize Filter Buttons
        filterGemuese = view.findViewById(R.id.gemuese_filter)
        filterObst = view.findViewById(R.id.obst_filter)
        filterFleisch = view.findViewById(R.id.fleisch_filter)
        filterGetreide = view.findViewById(R.id.getreide_filter)

        // Set OnClickListeners for Filter Buttons to filter the food list by category.
        filterGemuese.setOnClickListener { onFilterButtonClick(it as Button, "GEMUESE") }
        filterObst.setOnClickListener { onFilterButtonClick(it as Button, "OBST") }
        filterFleisch.setOnClickListener { onFilterButtonClick(it as Button, "FLEISCH") }
        filterGetreide.setOnClickListener { onFilterButtonClick(it as Button, "GETREIDE") }

        return view
    }

    /**
     * Loads all available food data from the FoodController into the RecyclerView adapter.
     * Updates the local map of food items.
     */
    private fun loadFoodData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                foodController.getAllFood().collect { foods ->
                    foodMap = foods.associateBy { it.food }
                    foodAdapter.updateData(foods)
                }
            } catch (e: CancellationException) {
                Log.w("StockFragment", "Data loading cancelled", e)
            } catch (e: Exception) {
                Log.e("StockFragment", "Failed to load food data", e)
            }
        }
    }

    /**
     * Applies the current query and category filter to the food items and updates the RecyclerView adapter.
     */
    private fun applyFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Filter food by current query and category
                foodController.getFilteredFood(currentQuery, currentCategory).collect { filteredFoods ->
                    foodAdapter.updateData(filteredFoods)
                }
            } catch (e: Exception) {
                Log.e("StockFragment", "Failed to filter food", e)
            }
        }
    }

    /**
     * Handles the selection of a filter button.
     *
     * @param button The button that was clicked.
     * @param category The food category associated with the button.
     */
    private fun onFilterButtonClick(button: Button, category: String) {
        if (selectedButton == button) {
            // Deselect the button if it's already selected
            button.setBackgroundResource(R.drawable.btn_use_filter)
            selectedButton = null
            currentCategory = null
        } else {
            // Update selection and apply category filter
            setSelectedButton(button)
            currentCategory = category
            foodAdapter.resetQuantityMap()
        }
        applyFilters()
    }

    /**
     * Sets the selected button and updates the button's background.
     *
     * @param button The button to be set as selected.
     */
    private fun setSelectedButton(button: Button) {
        // Reset the background of the previously selected button
        selectedButton?.setBackgroundResource(R.drawable.btn_use_filter)
        button.setBackgroundResource(R.drawable.btn_use_filter_selected)
        selectedButton = button
    }
}

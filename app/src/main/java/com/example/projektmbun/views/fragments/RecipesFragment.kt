package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektmbun.R
import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.databinding.FragmentRecipesBinding
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.SpaceItemDecoration
import com.example.projektmbun.utils.addSearchListener
import com.example.projektmbun.views.adapters.RecipeAdapter
import com.github.clans.fab.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!
    private var searchJob: Job? = null
    private lateinit var recipeController: RecipeController
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var foodCardDao: FoodCardDao
    private lateinit var addRecipeButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        val view = binding.root

        val recipeService = RecipeService()
        // Initialisiere RecipeController und FoodCardDao
        recipeController = RecipeController(recipeService)
        foodCardDao = AppDatabase.getDatabase(requireContext()).foodCardDao()

        // Initialisiere RecipeAdapter
        recipeAdapter = RecipeAdapter(emptyList())

        // RecyclerView setup
        binding.recipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
            addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.item_spacing)))
        }

        binding.searchBar.searchEditText.hint = "Rezepte suchen..."

        val filterIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter)
        binding.searchBar.scanIcon.setImageDrawable(filterIcon)

        binding.searchBar.scanIcon.setOnClickListener {
            toggleFilter()
        }

        // Modifizieren Sie den searchListener
        binding.searchBar.searchEditText.addSearchListener(lifecycleScope) { query ->
            searchJob?.cancel() // Cancel previous job if exists
            searchJob = lifecycleScope.launch(Dispatchers.IO) {
                if (!isActive) return@launch // Check if still active
                val results = recipeController.getRecipesByTitle(query)
                withContext(Dispatchers.Main) {
                    _binding?.let { // Null-Safety-Check
                        updateRecyclerView(results)
                    }
                }
            }
        }

        addRecipeButton = binding.addRecipe
        addRecipeButton.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_fragment_recipes_to_fragment_create_recipe,
                null,
                navOptions {
                    popUpTo(R.id.fragment_recipes) { inclusive = false }
                }
            )
        }

        // Lade die Rezepte basierend auf den FoodCards des Benutzers
        lifecycleScope.launch(Dispatchers.IO) {
            if (!isActive) return@launch
            val userFoodCards = foodCardDao.getAllFoodCards()

            try {
                val results = recipeController.getFilteredRecipes(userFoodCards)
                withContext(Dispatchers.Main) {
                    _binding?.let { binding -> // Null-Safety-Check
                        if (results.isNotEmpty()) {
                            binding.recipeRecyclerView.visibility = View.VISIBLE
                            binding.recipeErrorText.visibility = View.GONE
                            updateRecyclerView(results)
                        } else {
                            binding.recipeRecyclerView.visibility = View.GONE
                            binding.recipeErrorText.text = "Keine Rezepte gefunden"
                            binding.recipeErrorText.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _binding?.let { binding -> // Null-Safety-Check
                        binding.recipeRecyclerView.visibility = View.GONE
                        binding.recipeErrorText.text = "Ein Fehler ist aufgetreten"
                        binding.recipeErrorText.visibility = View.VISIBLE
                    }
                }
                e.printStackTrace()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        searchJob?.cancel() // Cancel any running job
        super.onDestroyView()
        _binding = null
    }

    private fun updateRecyclerView(recipeList: List<Recipe>) {
        recipeAdapter.updateData(recipeList)
    }


    private fun toggleFilter() {
        val filterLayout =
            requireView().findViewById<View>(R.id.filter_menu) // Referenz auf das separate Layout
        if (filterLayout.visibility == View.VISIBLE) {
            filterLayout.visibility = View.GONE
        } else {
            filterLayout.visibility = View.VISIBLE
        }
    }
}

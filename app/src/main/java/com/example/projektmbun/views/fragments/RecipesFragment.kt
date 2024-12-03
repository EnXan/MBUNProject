package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projektmbun.R
import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.databinding.FragmentRecipesBinding
import com.example.projektmbun.models.daos.FoodCardDao
import com.example.projektmbun.models.data.recipe.RecipeWithDishTypesAndIngredientsAndInstructions
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.SpaceItemDecoration
import com.example.projektmbun.utils.addSearchListener
import com.example.projektmbun.views.adapters.RecipeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeController: RecipeController
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var foodCardDao: FoodCardDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialisiere RecipeController und FoodCardDao
        val recipeDao = AppDatabase.getDatabase(requireContext()).recipeDao()
        recipeController = RecipeController(recipeDao)
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

        binding.searchBar.searchEditText.addSearchListener(lifecycleScope) { query ->
            lifecycleScope.launch(Dispatchers.IO) {
                val results = recipeController.getRecipesByName(query)
                withContext(Dispatchers.Main) {
                    updateRecyclerView(results)
                }
            }
        }

        // Lade die Rezepte basierend auf den FoodCards des Benutzers
        lifecycleScope.launch(Dispatchers.IO) {
            val userFoodCards = foodCardDao.getAllFoodCards() // Abfrage der FoodCards aus der lokalen DB

            try {
                val results = recipeController.getAvailableRecipes(userFoodCards)
                withContext(Dispatchers.Main) {
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
            } catch (e: Exception) {
                // Fehlerbehandlung: Leere Liste und Fehlermeldung anzeigen
                withContext(Dispatchers.Main) {
                    binding.recipeRecyclerView.visibility = View.GONE
                    binding.recipeErrorText.text = "Ein Fehler ist aufgetreten"
                    binding.recipeErrorText.visibility = View.VISIBLE
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateRecyclerView(recipeList: List<RecipeWithDishTypesAndIngredientsAndInstructions>) {
        recipeAdapter.updateData(recipeList)
    }
}

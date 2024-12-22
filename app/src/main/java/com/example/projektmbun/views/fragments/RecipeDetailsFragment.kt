package com.example.projektmbun.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projektmbun.controller.RecipeController
import com.example.projektmbun.databinding.FragmentRecipeDetailsBinding
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.data_structure.recipe.Ingredient
import com.example.projektmbun.models.data_structure.recipe.Instructions
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.enums.UnitsEnum
import com.example.projektmbun.views.adapters.RecipeIngredientsAdapter
import com.example.projektmbun.views.adapters.RecipeInstructionsAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!

    private var count: Int = 1
    private var ingredients: List<Ingredient> = listOf()
    private var instructions: List<Instructions> = listOf()

    private lateinit var ingredientsAdapter: RecipeIngredientsAdapter
    private lateinit var instructionsAdapter: RecipeInstructionsAdapter
    private lateinit var recipeController: RecipeController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        setupController()
        loadRecipeData()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupController() {
        val recipeService = RecipeService()
        recipeController = RecipeController(recipeService)
    }

    private fun loadRecipeData() {
        val recipeId = arguments?.getInt("RECIPE_ID")
        recipeId?.let {
            lifecycleScope.launch {
                val recipe = withContext(Dispatchers.IO) {
                    recipeController.getRecipeById(it)
                }
                if (recipe != null) {
                    ingredients = withContext(Dispatchers.IO) {
                        Log.d("RecipeDetailsFragment", "Ingredients: $ingredients")
                        recipeController.getIngredientsByRecipeId(it)
                    }
                    instructions = withContext(Dispatchers.IO) {
                        recipeController.getInstructionsByRecipeId(it)
                    }
                    setupUI(recipe)
                } else {
                    Log.e("RecipeDetailsFragment", "Recipe is null!")
                }
            }
        }
    }

    private suspend fun setupUI(recipe: Recipe) {
        val ingredientsWithAvailability = checkIngredientAvailability(ingredients)

        withContext(Dispatchers.Main) {
            setupIngredientsRecyclerView(ingredientsWithAvailability)
            setupInstructionsRecyclerView(instructions)

            binding.recipeName.text = recipe.title
            binding.recipeDescription.text = recipe.shortDescription
            loadRecipeImage(recipe.imageUrl)

            setupButtons()
        }
    }

    private fun setupIngredientsRecyclerView(ingredientsWithAvailability: List<Pair<Ingredient, Boolean>>) {
        ingredients = ingredientsWithAvailability.map { it.first }
        ingredientsAdapter = RecipeIngredientsAdapter(ingredientsWithAvailability)
        binding.ingredientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientsRecyclerView.adapter = ingredientsAdapter
    }

    private fun setupInstructionsRecyclerView(instructions: List<Instructions>) {
        instructionsAdapter = RecipeInstructionsAdapter(instructions)
        binding.instructionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.instructionsRecyclerView.adapter = instructionsAdapter
    }

    private fun loadRecipeImage(imageUrl: String) {
        if (imageUrl.isNotEmpty()) {
            Glide.with(binding.recipeImage.context)
                .load(imageUrl)
                .transform(RoundedCornersTransformation(20, 0))
                .into(binding.recipeImage)
        } else {
            Log.e("RecipeDetailsFragment", "Image URL is null or empty")
        }
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.buttonPlus.setOnClickListener {
            count++
            updatePortionText()
            updateServings()
        }

        binding.buttonMinus.setOnClickListener {
            if (count > 1) {
                count--
                updatePortionText()
                updateServings()
            }
        }
    }

    private fun updatePortionText() {
        binding.portionText.text = if (count == 1) {
            "$count Portion"
        } else {
            "$count Portionen"
        }
    }

    private fun updateServings() {
        lifecycleScope.launch {
            val updatedIngredientsAmount = ingredients.map { ingredient ->
                val newAmount = when (ingredient.unit) {
                    UnitsEnum.STUECK, UnitsEnum.UNBEKANNT -> ingredient.amount ?: 0.0 // Stück bleibt unverändert
                    else -> (ingredient.amount ?: 0.0) * count // Normal multiplizieren
                }
                ingredient.copy(amount = newAmount)
            }

            val updatedIngredientsWithAvailability = withContext(Dispatchers.IO) {
                checkIngredientAvailability(updatedIngredientsAmount)
            }

            val uiIngredients = updatedIngredientsWithAvailability.map { (ingredient, isAvailable) ->
                val (displayAmount, displayUnit) = ingredient.unit.convertToLarger(
                    ingredient.unit.convertToBase(ingredient.amount ?: 0.0)
                )
                ingredient.copy(amount = displayAmount, unit = displayUnit) to isAvailable
            }

            withContext(Dispatchers.Main) {
                ingredientsAdapter.updateIngredients(uiIngredients)
            }
        }
    }


    private suspend fun checkIngredientAvailability(ingredients: List<Ingredient>): List<Pair<Ingredient, Boolean>> {
        // Hole alle FoodCards aus der Datenbank
        val availableFoodCards = withContext(Dispatchers.IO) {
            AppDatabase.getDatabase(requireContext()).foodCardDao().getAllFoodCards()
        }

        // Gruppiere FoodCards nach foodId und summiere die Mengen in der Basiseinheit
        val mergedFoodCards = availableFoodCards
            .groupBy { it.foodId }
            .mapValues { (_, foodCards) ->
                foodCards.fold(0.0) { total, foodCard ->
                    total + foodCard.unit.convertToBase(foodCard.quantity)
                }
            }

        return ingredients.map { ingredient ->
            // Hole die verfügbare Menge aus den zusammengeführten FoodCards
            val availableAmountInBaseUnit = mergedFoodCards[ingredient.foodId.toString()] ?: 0.0

            val isAvailable = when (ingredient.unit) {
                UnitsEnum.UNBEKANNT -> false // Mengenprüfung für unbekannte Einheiten nicht möglich
                UnitsEnum.STUECK -> availableAmountInBaseUnit >= (ingredient.amount ?: 0.0)
                else -> {
                    // Konvertiere die benötigte Menge in die Basiseinheit
                    val requiredAmountInBaseUnit = ingredient.unit.convertToBase(ingredient.amount ?: 0.0)
                    availableAmountInBaseUnit >= requiredAmountInBaseUnit
                }
            }

            ingredient to isAvailable
        }
    }



}

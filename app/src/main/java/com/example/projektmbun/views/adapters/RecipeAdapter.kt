package com.example.projektmbun.views.adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.databinding.RecipePreviewBinding
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.utils.GlideApp
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.util.Locale

class RecipeAdapter(
    private var recipes: List<Recipe>,
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    // ViewHolder class to hold and bind each recipe item's views
    class ViewHolder(private val binding: RecipePreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        private val titleTextView: TextView = binding.titleTextView
        private val descriptionTextView: TextView = binding.descriptionTextView
        private val recipeImageView: ImageView = binding.recipeImageView
        private val durationTextView: TextView = binding.recipeDuration
        private val nutritionTextView: TextView = binding.nutritionTextView
        private val timeTextView: TextView = binding.recipeTime
        private val priceTextView: TextView = binding.recipePrice
        private val recipeCard: CardView = binding.recipeCard


        // Bind recipe data to views
        fun bind(recipe: Recipe, adapter: RecipeAdapter) {
            val recipe = recipe

            titleTextView.text = recipe.title
            nutritionTextView.text = adapter.checkNutrition(recipe)
            descriptionTextView.text = recipe.shortDescription
            durationTextView.text = "${recipe.readyInMinutes} min"
            timeTextView.text = recipe.dishType
            priceTextView.text = String.format(Locale.getDefault(), "%.2f €", recipe.pricePerServing).replace('.', ',')

            // Load recipe image using Glide
            GlideApp.with(recipeImageView.context)
                .load(recipe.imageUrl)
                .transform(RoundedCornersTransformation(20, 0))
                .into(recipeImageView)

            // Set click listener to navigate to recipe details fragment
            recipeCard.setOnClickListener {
                val recipeId = recipe.id
                val bundle = Bundle().apply {
                    if (recipeId != null) {
                        putInt("RECIPE_ID", recipeId)
                    }
                }

                it.findNavController().navigate(
                    R.id.action_recipes_fragment_to_recipe_details_fragment,
                    bundle,
                    navOptions {
                        launchSingleTop = true
                    }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecipePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipeWithDetails = recipes[position]
        holder.bind(recipeWithDetails, this)
    }

    override fun getItemCount(): Int = recipes.size

    // Update the recipes data and refresh the adapter
    fun updateData(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    fun checkNutrition(recipe: Recipe): String {
        return when {
            recipe.vegan -> "Vegan"
            recipe.vegetarian -> "Vegetarisch"
            recipe.pescetarian -> "Pescetarisch"
            recipe.dairyFree -> "Laktosefrei"
            recipe.glutenFree -> "Glutenfrei"
            else -> "Standard"
        }
    }
}

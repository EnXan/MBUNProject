package com.example.projektmbun.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.databinding.ItemIngredientBinding
import com.example.projektmbun.models.data.recipe.Ingredient
import com.example.projektmbun.utils.Converters

class RecipeIngredientsAdapter(
    private var ingredients: List<Pair<Ingredient, Boolean>>
) : RecyclerView.Adapter<RecipeIngredientsAdapter.ViewHolder>() {

    // ViewHolder-Klasse
    class ViewHolder(private val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredientWithAvailability: Pair<Ingredient, Boolean>) {
            val (ingredient, isAvailable) = ingredientWithAvailability

            // Setze den Namen der Zutat
            binding.ingredientText.text = ingredient.name

            // Verfügbarkeit anzeigen (grün/rot)
            binding.ingredientText.setTextColor(
                binding.root.context.getColor(
                    if (isAvailable) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                )
            )

            // Menge und Einheit anzeigen
            if (ingredient.amount == null || ingredient.amount == 0.0) {
                binding.quantityText.visibility = View.GONE
                binding.quantityUnit.visibility = View.GONE
            } else {
                binding.quantityText.visibility = View.VISIBLE
                binding.quantityUnit.visibility = View.VISIBLE
                binding.quantityText.text = ingredient.amount.toString()
                binding.quantityUnit.text = Converters.getUnitTextFromEnum(ingredient.unit)
            }
        }
    }

    // Erstelle den ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Binde Daten an den ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    // Aktualisiere die Liste der Zutaten
    fun updateIngredients(newIngredients: List<Pair<Ingredient, Boolean>>) {
        ingredients = newIngredients
        notifyDataSetChanged()
    }

    // Anzahl der Elemente
    override fun getItemCount(): Int = ingredients.size
}

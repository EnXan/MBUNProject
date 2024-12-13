package com.example.projektmbun.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.databinding.ItemFoodSmallDarkBinding
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import com.example.projektmbun.utils.Converters


/**
 * RecyclerView Adapter for displaying food items in the FoodFragment.
 * Each food item has an option for setting an expiration date and a quantity using date and quantity pickers.
 *
 * @constructor Creates a FoodCardAdapter with a given set of food items.
 * @param foodCardSet Array of FoodCardWithFood objects to display in the RecyclerView.
 */
class FoodCardMenuAdapter(private var foodCardSet: List<FoodCardWithDetails>) :
    RecyclerView.Adapter<FoodCardMenuAdapter.ViewHolder>() {

    /**
     * A map to store the selected date and quantity for each food item.
     * The key is the food name, and the value is an instance of SelectedFoodCardHolder.
     */
    private val selectedFoodCardMap = mutableMapOf<String, SelectedFoodCardHolder>()

    /**
     * ViewHolder class for the FoodCardAdapter.
     * Holds references to the views for each item without binding data yet.
     *
     * @property binding The view binding for each item in the RecyclerView.
     */
    inner class ViewHolder(private val binding: ItemFoodSmallDarkBinding) : RecyclerView.ViewHolder(binding.root) {

        private var foodTitleText = binding.foodTitle
        private var foodCategoryText = binding.foodCategory

        /**
         * Binds the food item data to the views, and sets up listeners for the date and quantity selectors.
         *
         * @param foodCardWithFood The FoodCardWithFood item being bound to this ViewHolder.
         */
        fun bind(
            foodCardWithFood: FoodCardWithDetails,
        ) {
            val foodName = foodCardWithFood.food.name
            val category = foodCardWithFood.food.category

            // Set food data to the view
            foodTitleText.text = foodName
            foodCategoryText.text = Converters.getCategoryTextFromEnum(category)

            binding.buttonLinkStockList.setOnClickListener {
                it.findNavController().navigate(
                    R.id.action_fragment_menu_to_fragment_stock_list,
                    null,
                    navOptions {
                        popUpTo(R.id.fragment_menu) { inclusive = true }
                    }
                )
            }
        }
    }

    /**
     * Creates a new ViewHolder when there are no existing ViewHolders available to reuse.
     *
     * @param viewGroup The parent view that will hold the ViewHolder.
     * @param viewType The type of view to create.
     * @return A new ViewHolder instance.
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodSmallDarkBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    /**
     * Binds the food item data and selected date and quantity from the map to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the food item in the adapter.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foodCardWithDetails = foodCardSet[position]

        holder.bind(foodCardWithDetails)
    }

    /**
     * Updates the food set and notifies the adapter that the data has changed.
     * @param newFoodSet new list of foods, displayed in the adapter
     */
    fun updateData(newFoodSet: List<FoodCardWithDetails>) {
        foodCardSet = newFoodSet
        selectedFoodCardMap.clear()
        notifyDataSetChanged()
    }

    /**
     * Returns the total number of food items in the adapter.
     *
     * @return The size of the foodCardSet array.
     */
    override fun getItemCount(): Int {
        return foodCardSet.size
    }
}

package com.example.projektmbun.views.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.databinding.ItemExpiryDateBinding
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import com.example.projektmbun.utils.Converters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


/**
 * RecyclerView Adapter for displaying food items in the FoodFragment.
 * Each food item has an option for setting an expiration date and a quantity using date and quantity pickers.
 *
 * @constructor Creates a FoodCardAdapter with a given set of food items.
 * @param foodCardSet Array of FoodCardWithFood objects to display in the RecyclerView.
 */
class ExpiryDateMenuAdapter(private var foodCardSet: List<FoodCardWithDetails>) :
    RecyclerView.Adapter<ExpiryDateMenuAdapter.ViewHolder>() {

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
    inner class ViewHolder(private val binding: ItemExpiryDateBinding) : RecyclerView.ViewHolder(binding.root) {

        private val foodTitleText: TextView = binding.foodTitle
        private val foodCategoryText: TextView = binding.foodCategory
        private val expiryDateText: TextView = binding.expiryDate
        private val itemExpiryDateLayout: LinearLayout = binding.itemExpiryDate

        /**
         * Binds the food item data to the views, and sets up listeners for the date and quantity selectors.
         *
         * @param foodCardWithFood The FoodCardWithFood item being bound to this ViewHolder.
         */

        fun bind(
            foodCardWithFood: FoodCardWithDetails,
        ) {
            val foodName = foodCardWithFood.foodLocal.name
            val expiryDate = foodCardWithFood.foodCard.expiryDate
            val category = foodCardWithFood.foodLocal.category

            // Set food data to the view
            foodTitleText.text = foodName
            foodCategoryText.text = Converters.fromCategoryEnum(category)
            expiryDateText.text = expiryDate


            // Set tint color on drawable based on expiry date
            val cardBackground = itemExpiryDateLayout
            val drawable =
                ContextCompat.getDrawable(cardBackground.context, R.drawable.expiry_date_background)
                    ?.let {
                        DrawableCompat.wrap(it)
                    }


            expiryDate?.let {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val today = Calendar.getInstance().time
                val expiryDateParsed = sdf.parse(it)

                if (expiryDateParsed != null) {
                    val diff = expiryDateParsed.time - today.time
                    val daysUntilExpiry = (diff / (1000 * 60 * 60 * 24)).toInt()

                    // Calculate color based on daysUntilExpiry
                    val greenValue = when {
                        daysUntilExpiry > 30 -> 255 // Fully green for dates more than 30 days away
                        daysUntilExpiry > 0 -> (daysUntilExpiry / 30.0 * 255).toInt() // Gradual transition
                        else -> 0 // Red if expired or today
                    }
                    val redValue = when {
                        daysUntilExpiry > 30 -> 0 // No red for dates more than 30 days away
                        daysUntilExpiry > 0 -> (255 - (daysUntilExpiry / 30.0 * 255)).toInt() // Gradual transition
                        else -> 255 // Fully red if expired or today
                    }

                    val color = Color.rgb(redValue, greenValue, 0)
                    drawable?.setTint(color) // Setzt die berechnete Farbe auf das Drawable
                }
            } ?: run {
                // Default tint color for null expiry date
                drawable?.setTint(ContextCompat.getColor(cardBackground.context, R.color.gray))
            }

            // Set the tinted drawable back to the background
            cardBackground.background = drawable
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
        val binding = ItemExpiryDateBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
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

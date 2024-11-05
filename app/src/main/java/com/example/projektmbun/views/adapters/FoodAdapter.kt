package com.example.projektmbun.views.adapters

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.models.data.Food
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Adapter class for displaying a list of Food items in a RecyclerView.
 *
 * @param foodList List of Food items to display.
 * @param context Context used to initialize dialogs and access resources.
 */
class FoodAdapter(
    private var foodList: List<Food?>,
    private val context: Context
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    /**
     * ViewHolder class that holds references to the UI components in each item view.
     *
     * @param itemView The root view of the item layout.
     */
    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.foodTitle)
        val foodCategory: TextView = itemView.findViewById(R.id.foodCategory)
        val btnSelectDate: Button = itemView.findViewById(R.id.btn_select_date)
        val btnSelectQuantity: Button = itemView.findViewById(R.id.btn_select_menge)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new FoodViewHolder holding the view for each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder to be updated with the data.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        if (food == null) {
            Log.e("FoodAdapter", "Food item at position $position is null")
        }
        holder.foodName.text = food?.food ?: "Unknown food name"
        holder.foodCategory.text = convertCategory(food?.category?.name ?: "Unknown category")

        // Set up the date picker dialog on the button
        holder.btnSelectDate.setOnClickListener {
            try {
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        holder.btnSelectDate.text =
                            dateFormat.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            } catch (e: Exception) {
                Log.e("FoodAdapter", "Error opening DatePickerDialog: ${e.message}")
            }
        }

        // Set up the number picker dialog on the quantity button
        holder.btnSelectQuantity.setOnClickListener {
            try {
                // array with values in steps of 25
                val displayedValues = Array(41) { i -> "${i * 25} g" }

                val numberPicker = NumberPicker(context).apply {
                    minValue = 0
                    maxValue = displayedValues.size - 1
                    value = 4 // Standardwert entspricht 100 g
                    wrapSelectorWheel = false
                    setDisplayedValues(displayedValues)
                }

                AlertDialog.Builder(context)
                    .setTitle("Menge auswählen")
                    .setView(numberPicker)
                    .setPositiveButton("OK") { _, _ ->
                        val selectedValue = numberPicker.value * 25
                        val quantityText = context.getString(R.string.quantity_text, selectedValue)
                        holder.btnSelectQuantity.text = quantityText
                    }
                    .setNegativeButton("Abbrechen", null)
                    .show()
            } catch (e: Exception) {
                Log.e("FoodAdapter", "Error in NumberPicker dialog: ${e.message}")
            }
        }

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The number of items in foodList.
     */
    override fun getItemCount(): Int = foodList.size

    /**
     * Converts the category name from a database code to a readable format for display.
     *
     * @param category The category name from the database.
     * @return A readable category name in German.
     */
    private fun convertCategory(category: String): String {
        return when (category) {
            "OBST" -> "Obst"
            "GEMUESE" -> "Gemüse"
            "FLEISCH" -> "Fleisch"
            "GETREIDE" -> "Getreide"
            "MILCHPRODUKT" -> "Milchprodukt"
            "FISCH" -> "Fisch"
            "HUELSENFRUCHT" -> "Hülsenfrucht"
            "SAMEN" -> "Samen"
            "NUSS" -> "Nuss"
            "OEL" -> "Öl"
            "FETT" -> "Fett"
            "EI" -> "Ei"
            "GEWUERZ" -> "Gewürz"
            else -> category
        }
    }

    /**
     * Updates the data in the adapter and refreshes the view.
     *
     * @param newFoodList The new list of Food items.
     */
    fun updateData(newFoodList: List<Food>) {
        foodList = newFoodList
        notifyDataSetChanged() // Refreshes the entire list; consider using DiffUtil for efficiency
    }
}

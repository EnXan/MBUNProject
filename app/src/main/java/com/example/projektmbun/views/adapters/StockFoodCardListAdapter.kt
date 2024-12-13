package com.example.projektmbun.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.databinding.ItemFoodAddedBinding
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.data_structure.food_card.FoodCardWithDetails
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.enums.UnitsEnum
import com.example.projektmbun.views.fragments.DatePickerFragment
import com.example.projektmbun.views.fragments.QuantityPickerFragment
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Data class to hold the selected date and quantity for a food item.
 * This serves as a temporary data model for food items in the `StockFoodCardListAdapter`.
 *
 * @property selectedDate The selected expiration date for the food item.
 * @property quantity The selected quantity of the food item.
 */
data class SelectedFoodCardHolder(val selectedDate: String? = null, val quantity: String? = null)

/**
 * RecyclerView Adapter for displaying food items in the StockFragment.
 * Each food item has an option for setting an expiration date and a quantity using date and quantity pickers.
 *
 * @constructor Creates a StockFoodCardListAdapter with a given set of food items.
 * @param foodCardSet Array of FoodCardWithDetails objects to display in the RecyclerView.
 */
class StockFoodCardListAdapter(private var foodCardSet: List<FoodCardWithDetails>, context: Context) :
    RecyclerView.Adapter<StockFoodCardListAdapter.ViewHolder>() {

    private val selectedFoodCardMap = mutableMapOf<String, SelectedFoodCardHolder>()
    private lateinit var foodCardDao: FoodCardDao
    private lateinit var foodCardController: FoodCardController
    private lateinit var foodService: FoodService

    init {
        foodCardDao = AppDatabase.getDatabase(context).foodCardDao()
        foodService = FoodService()
        foodCardController = FoodCardController(foodCardDao, foodService)

    }

    /**
     * ViewHolder class for the StockFoodCardListAdapter.
     * Holds references to the views for each item without binding data yet.
     *
     * @property binding The view binding for each item in the RecyclerView.
     */
    inner class ViewHolder(private val binding: ItemFoodAddedBinding) : RecyclerView.ViewHolder(binding.root) {
        private var btnSelectedDateView: Button = binding.btnSelectDate
        private var txtQuantityView: TextView = binding.btnSelectQuantity
        private var btnDeleteFoodCard: ImageButton = binding.btnDeleteFood

        fun bind(
            foodCardWithDetails: FoodCardWithDetails,
            dataHolder: SelectedFoodCardHolder?,
            onDataUpdated: (String, (SelectedFoodCardHolder) -> SelectedFoodCardHolder) -> Unit,
            foodCardController: FoodCardController
        ) {
            val foodCard = foodCardWithDetails.foodCard
            val foodName = foodCardWithDetails.food.name
            var selectedDate = dataHolder?.selectedDate ?: itemView.context.getString(R.string.placeholder_expiryDate)
            var selectedQuantity = dataHolder?.quantity
                ?: "${foodCard.quantity} ${Converters.getUnitTextFromEnum(foodCard.unit)}"
            Log.d("StockFoodCardAdapter", "unit: ${foodCard.unit}")

            // Set food data to the view
            binding.foodTitle.text = foodName
            binding.foodCategory.text = foodCardWithDetails.food.category.name
            btnSelectedDateView.text = selectedDate
            txtQuantityView.text = selectedQuantity

            // Date picker
            btnSelectedDateView.setOnClickListener {
                val fragmentManager = (itemView.context as? AppCompatActivity)?.supportFragmentManager

                if (fragmentManager != null) {
                    val selectedDateParts = selectedDate.split(".")
                    val lastDay = selectedDateParts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    val lastMonth = (selectedDateParts.getOrNull(1)?.toIntOrNull()?.minus(1)) ?: Calendar.getInstance().get(Calendar.MONTH)
                    val lastYear = selectedDateParts.getOrNull(2)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)

                    val datePickerFragment = DatePickerFragment(
                        listener = { year, month, day ->
                            selectedDate = "$day.${month + 1}.$year"
                            btnSelectedDateView.text = selectedDate

                            // Update expiry date in the database
                            (itemView.context as? AppCompatActivity)?.lifecycleScope?.launch {
                                foodCard.id?.let { foodCardController.updateExpiryDateByFoodCardId(it, selectedDate) }
                            }

                            // Update selected date in the adapter
                            onDataUpdated(foodName) { currentHolder ->
                                currentHolder.copy(selectedDate = selectedDate)
                            }
                        },
                        defaultYear = lastYear,
                        defaultMonth = lastMonth,
                        defaultDay = lastDay
                    )
                    datePickerFragment.show(fragmentManager, "datePicker")
                }
            }

            // Quantity picker
            txtQuantityView.setOnClickListener {
                val fragmentManager = (itemView.context as? AppCompatActivity)?.supportFragmentManager

                if (fragmentManager != null) {
                    val quantityParts = selectedQuantity.split(" ")
                    val lastQuantity = quantityParts.firstOrNull()?.toDoubleOrNull() ?: 1.0

                    // Stelle sicher, dass foodCard.unit ein UnitsEnum ist
                    val lastUnitEnum = foodCard.unit

                    val quantityPickerFragment = QuantityPickerFragment(
                        listener = { quantity, measureUnit ->
                            val unitEnum = try {
                                measureUnit?.let { UnitsEnum.valueOf(it.uppercase()) } ?: UnitsEnum.STUECK
                            } catch (e: IllegalArgumentException) {
                                UnitsEnum.UNITLESS // Fallback auf UNITLESS bei ungültigem String
                            }

                            // Einheit so formatieren wie im zweiten Codeblock
                            selectedQuantity = when (measureUnit) {
                                null, " " -> {
                                    // Keine Einheit, zeige nur die Menge
                                    "${quantity?.toInt()}"
                                }
                                "g" -> {
                                    // Wenn die Einheit "g" ist, zeige ohne Dezimalstellen
                                    "${quantity?.toInt()} $measureUnit"
                                }
                                else -> {
                                    // Standardanzeige
                                    "$quantity $measureUnit"
                                }
                            }

                            txtQuantityView.text = selectedQuantity

                            onDataUpdated(foodName) { currentHolder ->
                                currentHolder.copy(quantity = selectedQuantity)
                            }

                            // Speichere Änderungen in der Datenbank
                            (itemView.context as? AppCompatActivity)?.lifecycleScope?.launch {
                                foodCard.quantity = quantity ?: 0.0
                                foodCard.unit = unitEnum // Speichere UnitsEnum
                                foodCardController.addFoodCard(foodCard)
                            }
                        },
                        initialQuantityString = selectedQuantity,
                        foodState = foodCardWithDetails.food.state
                    )
                    quantityPickerFragment.show(fragmentManager, "quantityPicker")
                }
            }


            // Delete food card
            btnDeleteFoodCard.setOnClickListener {
                val fragmentScope = (itemView.context as? AppCompatActivity)?.lifecycleScope
                fragmentScope?.launch {
                    foodCard.id?.let {
                        foodCardController.deleteFoodCardById(it)
                    }

                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val updatedFoodCardSet = foodCardSet.toMutableList()
                        updatedFoodCardSet.removeAt(position)
                        updateData(updatedFoodCardSet)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodAddedBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foodCardWithDetails = foodCardSet[position]
        val selectedFoodCardHolder = selectedFoodCardMap[foodCardWithDetails.food.name] ?: SelectedFoodCardHolder()

        holder.bind(foodCardWithDetails, selectedFoodCardHolder, { foodName, updateFunction ->
            val currentHolder = selectedFoodCardMap[foodName] ?: SelectedFoodCardHolder()
            val updatedHolder = updateFunction(currentHolder)
            selectedFoodCardMap[foodName] = updatedHolder
        }, foodCardController)
    }

    fun updateData(newFoodSet: List<FoodCardWithDetails>) {
        foodCardSet = newFoodSet
        selectedFoodCardMap.clear()

        newFoodSet.forEach { foodCardWithDetails ->
            val foodCard = foodCardWithDetails.foodCard
            val expiryDate = foodCard.expiryDate ?: "Haltbar bis"

            selectedFoodCardMap[foodCardWithDetails.food.name] = SelectedFoodCardHolder(
                selectedDate = expiryDate,
                quantity = "${foodCard.quantity} ${Converters.getUnitTextFromEnum(foodCard.unit)}"
            )
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return foodCardSet.size
    }
}

package com.example.projektmbun.views.adapters

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.projektmbun.R
import com.example.projektmbun.controller.FoodCardController
import com.example.projektmbun.controller.RoutineController
import com.example.projektmbun.databinding.ItemFoodBinding
import com.example.projektmbun.models.cloud.service.FoodService
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.local.daos.RoutineDao
import com.example.projektmbun.models.local.daos.StockDao
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.database.AppDatabase
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.animations.Animations
import com.example.projektmbun.utils.enums.UnitsEnum
import com.example.projektmbun.views.fragments.DatePickerFragment
import com.example.projektmbun.views.fragments.QuantityPickerFragment
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Data class to hold the selected date and quantity for a food item.
 * This serves as a temporary data model for food items in the `FoodAdapter`.
 *
 * @property selectedDate The selected expiration date for the food item.
 * @property quantity The selected quantity of the food item.
 */
data class SelectedFoodHolder(val selectedDate: String? = null, val quantity: String? = null)

/**
 * RecyclerView Adapter for displaying food items in the FoodFragment.
 * Each food item has an option for setting an expiration date and a quantity using date and quantity pickers.
 *
 * @constructor Creates a FoodAdapter with a given set of food items.
 * @param foodLocalSet Array of Food objects to display in the RecyclerView.
 */
class FoodAdapter(private var foodLocalSet: List<FoodLocal>,
                  context: Context,
                  private val showFinishButton: Boolean,
                  private val routineId: Int?,
                  private val stockId: Int?
    ) :
    RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    /**
     * A map to store the selected date and quantity for each food item.
     * The key is the food name, and the value is an instance of SelectedFoodHolder.
     */
    private val selectedFoodMap = mutableMapOf<String, SelectedFoodHolder>()

    //All DAOs needed
    private var foodCardDao: FoodCardDao = AppDatabase.getDatabase(context).foodCardDao()
    private var routineDao: RoutineDao = AppDatabase.getDatabase(context).routineDao()

    private var foodService: FoodService = FoodService()

    private var foodCardController: FoodCardController = FoodCardController(foodCardDao, foodService)
    private var routineController: RoutineController = RoutineController(routineDao, foodCardDao)

    /**
     * ViewHolder class for the FoodAdapter.
     * Holds references to the views for each item without binding data yet.
     *
     * @property binding The view binding for each item in the RecyclerView.
     */
    inner class ViewHolder(private val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {

        private var foodTitleText: TextView = binding.foodTitle
        private var foodCategoryText: TextView = binding.foodCategory
        private var selectDateButton: Button = binding.btnSelectDate
        private var selectQuantityButton: Button = binding.btnSelectQuantity
        private var addToStockButton: ImageButton = binding.btnAddToStock
        /**
         * Binds the food item data to the views, and sets up listeners for the date and quantity selectors.
         *
         * @param foodLocal The food item being bound to this ViewHolder.
         * @param dataHolder Holds the selected date and quantity for the food item.
         * @param onDataUpdated Callback function for updating selected date and quantity in the adapter.
         */
        fun bind(
            foodLocal: FoodLocal,
            dataHolder: SelectedFoodHolder?,
            onDataUpdated: (String, (SelectedFoodHolder) -> SelectedFoodHolder) -> Unit,
            routineController: RoutineController,
        ) {

            val foodName = foodLocal.name
            val foodCategory = foodLocal.category

            var selectedDate = dataHolder?.selectedDate
            var selectedQuantity = dataHolder?.quantity



            selectDateButton.text = selectedDate ?: itemView.context.getString(R.string.placeholder_expiryDate)
            selectDateButton.visibility = if (showFinishButton) View.GONE else View.VISIBLE

            selectQuantityButton.text = "Wähle eine Menge"

            foodTitleText.text = foodName
            foodCategoryText.text = Converters.fromCategoryEnum(foodCategory)



            selectDateButton.setOnClickListener {
                val fragmentManager = (itemView.context as? AppCompatActivity)?.supportFragmentManager

                if (fragmentManager != null) {
                    val selectedDateParts = selectedDate?.split(".") ?: listOf("", "", "")
                    val lastDay = selectedDateParts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    val lastMonth = (selectedDateParts.getOrNull(1)?.toIntOrNull()?.minus(1)) ?: Calendar.getInstance().get(Calendar.MONTH)
                    val lastYear = selectedDateParts.getOrNull(2)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)


                    val datePickerFragment = DatePickerFragment(
                        listener = { year, month, day ->
                            selectedDate = "$day.${month + 1}.$year"
                            selectDateButton.text = selectedDate

                            // Update selected date in the adapter
                            onDataUpdated(foodLocal.name) { currentHolder ->
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


            selectQuantityButton.setOnClickListener {
                val fragmentManager = (itemView.context as? AppCompatActivity)?.supportFragmentManager

                if (fragmentManager != null) {
                    // Verwende den aktuell gespeicherten Wert für initialQuantityString
                    val initialQuantityString = selectedQuantity

                    val quantityPickerFragment = QuantityPickerFragment(
                        listener = { quantity, unit ->
                            val newQuantity = when {
                                unit == null || unit == UnitsEnum.UNBEKANNT -> "Fehler"
                                else -> "${quantity ?: 100} ${Converters.fromUnitEnum(unit)}"
                            }

                            // Update selectedQuantity und UI
                            selectedQuantity = newQuantity
                            selectQuantityButton.text = newQuantity

                            // Aktualisiere den Holder
                            onDataUpdated(foodLocal.name) { currentHolder ->
                                currentHolder.copy(quantity = newQuantity)
                            }
                        },
                        initialQuantityString = initialQuantityString // Übergabe des gespeicherten Wertes
                    )
                    quantityPickerFragment.show(fragmentManager, "quantityPicker")
                }
            }



            addToStockButton.setOnClickListener {


                if(selectedQuantity == null) {
                    Toast.makeText(itemView.context, "Bitte eine Menge auswählen", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Starte die Animation
                Animations.animateButton(addToStockButton)



                // Verarbeite das Hinzufügen zur Datenbank
                val quantityParts = selectedQuantity?.split(" ")
                val quantity = quantityParts?.firstOrNull()?.toDoubleOrNull() ?: 0.0
                val unitText = quantityParts?.getOrNull(1) ?: ""
                val unitEnum = Converters.toUnitEnum(unitText)

                val fragmentManager = (itemView.context as? AppCompatActivity)?.lifecycleScope

                fragmentManager?.launch {
                    val foodCard = FoodCard(
                        id = null,
                        foodId = foodName,
                        quantity = quantity,
                        unit = unitEnum,
                        isActive = false,
                        expiryDate = selectedDate.takeIf { it != itemView.context.getString(R.string.placeholder_expiryDate) },
                        stockId = null,
                        routineId = null,
                    )

                    try {
                        if (!showFinishButton) {
                            foodCard.stockId = stockId
                            foodCardController.addFoodCardToStock(foodCard, stockId)
                        } else {
                            foodCard.routineId = routineId
                            routineController.addFoodCardToRoutine(foodCard, routineId)
                        }
                    } catch (e: SQLiteConstraintException) {
                        Log.e("FoodAdapter", "SQLiteConstraintException: ${e.message}. FoodCard: $foodCard")
                        Toast.makeText(itemView.context, "SQLiteConstraintException", Toast.LENGTH_SHORT).show()
                    } catch (e: SQLiteException) {
                        Log.e("FoodAdapter", "SQLiteException: ${e.message}. FoodCard: $foodCard")
                        Toast.makeText(itemView.context, "SQLiteException", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("FoodAdapter", "Exception: ${e.message}. FoodCard: $foodCard")
                        Toast.makeText(itemView.context, "Exception occurred", Toast.LENGTH_SHORT).show()
                    }
                }
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
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    /**
     * Binds the food item data and selected date and quantity from the map to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the food item in the adapter.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foodLocalSet[position]
        val selectedFoodHolder = selectedFoodMap[food.name] ?: SelectedFoodHolder()

        holder.bind(food, selectedFoodHolder, { foodName, updateFunction ->
            val currentHolder = selectedFoodMap[foodName] ?: SelectedFoodHolder()
            val updatedHolder = updateFunction(currentHolder)
            selectedFoodMap[foodName] = updatedHolder
        }, routineController)
    }


    /**
     * Updates the food set and notifies the adapter that the data has changed.
     * @param newFoodLocalSet new list of foods, displayed in the adapter
     */
    fun updateData(newFoodLocalSet: List<FoodLocal>) {
        foodLocalSet = newFoodLocalSet
        Log.d("FoodAdapter", "Updated food set: $foodLocalSet")
        notifyDataSetChanged() // Benachrichtige die RecyclerView, dass sich die Daten geändert haben
    }

    /**
     * Returns the total number of food items in the adapter.
     *
     * @return The size of the foodSet array.
     */
    override fun getItemCount(): Int {
        return foodLocalSet.size
    }
}

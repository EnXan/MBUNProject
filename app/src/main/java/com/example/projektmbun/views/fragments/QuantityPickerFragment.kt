package com.example.projektmbun.views.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import com.aigestudio.wheelpicker.WheelPicker
import com.example.projektmbun.R
import com.example.projektmbun.databinding.QuantityPickerBinding
import com.example.projektmbun.utils.enums.FoodStateEnum

class QuantityPickerFragment(
    private val listener: (quantity: Double?, measureUnit: String?) -> Unit,
    private val initialQuantityString: String,
    private val foodState: FoodStateEnum
) : DialogFragment() {

    private var _binding: QuantityPickerBinding? = null
    private val binding get() = _binding!!

    private var lastSelectedUnitIndex: Int = 0
    private var lastSelectedGramsIndex: Int = 0

    companion object {
        private const val EMPTY_UNIT = "-"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = QuantityPickerBinding.inflate(layoutInflater)
        val view = binding.root

        val filteredUnits = getUnitsForCategory(foodState)
        val wheelPickerValue = view.findViewById<WheelPicker>(R.id.wheel_picker_value)
        val wheelPickerGrams = view.findViewById<WheelPicker>(R.id.wheel_picker_grams)
        val wheelPickerUnit = view.findViewById<WheelPicker>(R.id.wheel_picker_unit)

        // Parse initial quantity string
        val (initialQuantity, initialUnit, initialGrams) = parseInitialQuantityString(initialQuantityString)
        Log.d("QuantityPickerFragment", "Initial Unit Before Setup: $initialUnit")

        // Setup Pickers
        setupUnitPicker(wheelPickerUnit, filteredUnits, initialUnit)
        setupValuePicker(wheelPickerValue, initialUnit, initialQuantity)
        setupGramsPicker(wheelPickerGrams, initialGrams)
        updateGramsPickerVisibility(wheelPickerGrams, initialUnit)

        // Add listener for unit picker
        wheelPickerUnit.setOnItemSelectedListener { _, data, _ ->
            val selectedUnit = data.toString()
            Log.d("QuantityPickerFragment", "Selected Unit: $selectedUnit")

            // Update value picker based on selected unit
            setupValuePicker(wheelPickerValue, selectedUnit, 1.0)

            // Update visibility of grams picker
            updateGramsPickerVisibility(wheelPickerGrams, selectedUnit)
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val selectedValue = wheelPickerValue.data[wheelPickerValue.currentItemPosition].toString().toDouble()
                val selectedGrams = if (wheelPickerGrams.visibility == View.VISIBLE) {
                    wheelPickerGrams.data[wheelPickerGrams.currentItemPosition]
                        .toString()
                        .replace(",", ".")
                        .toDouble()
                } else {
                    0.0
                }
                val selectedUnit = wheelPickerUnit.data[wheelPickerUnit.currentItemPosition].toString()
                listener(selectedValue + selectedGrams, if (selectedUnit != EMPTY_UNIT) selectedUnit else null)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private fun setupUnitPicker(wheelPickerUnit: WheelPicker, units: List<String>, initialUnit: String) {
        wheelPickerUnit.data = units
        val initialPosition = units.indexOf(initialUnit).takeIf { it >= 0 } ?: units.indexOf(EMPTY_UNIT)
        wheelPickerUnit.post {
            wheelPickerUnit.selectedItemPosition = initialPosition.coerceAtLeast(0)
            Log.d("QuantityPickerFragment", "Set Unit Picker to Position: $initialPosition")
        }
    }

    private fun setupValuePicker(wheelPickerValue: WheelPicker, unit: String, initialQuantity: Double) {
        Log.d("QuantityPickerFragment", "Setup Value Picker - Unit: $unit, Initial Quantity: $initialQuantity")

        val values = when (unit) {
            "g", "ml"  -> (10..1000 step 10).map { it.toString() }
            "kg", "l" -> (1..10).map { it.toString() }
            EMPTY_UNIT -> (1..10).map { it.toString() }
            else -> emptyList()
        }
        wheelPickerValue.data = values

        val initialPosition = values.indexOf(initialQuantity.toInt().toString())
        Log.d("QuantityPickerFragment", "Initial Position for Quantity: $initialPosition")

        wheelPickerValue.post {
            wheelPickerValue.selectedItemPosition = initialPosition.coerceAtLeast(0)
            Log.d("QuantityPickerFragment", "Picker Position Set to: ${wheelPickerValue.selectedItemPosition}")
        }
    }

    private fun setupGramsPicker(wheelPickerGrams: WheelPicker, initialGrams: Double) {
        val gramsValues = (0..950 step 50).map { String.format(",%03d", it) }
        wheelPickerGrams.data = gramsValues

        // Runde den initialen Grammwert auf die nächsten gültigen Werte
        val roundedGrams = (initialGrams / 50).toInt() * 50
        val initialPosition = gramsValues.indexOf(String.format(",%03d", roundedGrams))

        Log.d("QuantityPickerFragment", "Initial Position for Grams: $initialPosition, Rounded Grams: $roundedGrams")

        wheelPickerGrams.post {
            wheelPickerGrams.selectedItemPosition = initialPosition.coerceAtLeast(0)
            Log.d("QuantityPickerFragment", "Grams Picker Position Set to: ${wheelPickerGrams.selectedItemPosition}")
        }
    }


    private fun updateGramsPickerVisibility(wheelPickerGrams: WheelPicker, unit: String) {
        if (unit == "kg" || unit == "l") {
            wheelPickerGrams.visibility = View.VISIBLE
            Log.d("QuantityPickerFragment", "Grams Picker Visible")
        } else {
            wheelPickerGrams.visibility = View.GONE
            Log.d("QuantityPickerFragment", "Grams Picker Hidden")
        }
    }


    private fun parseInitialQuantityString(quantityString: String): Triple<Double, String, Double> {
        val quantityParts = quantityString.trim().split(" ")
        Log.d("QuantityPickerFragment", "Quantity Parts: $quantityParts")

        val quantity = quantityParts.getOrNull(0)?.toDoubleOrNull() ?: 1.0
        Log.d("QuantityPickerFragment", "Just Quantity: $quantity")
        val unit = quantityParts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: EMPTY_UNIT
        Log.d("QuantityPickerFragment", "Unit: $unit")

        val grams = if (unit == "kg") (quantity - quantity.toInt()) * 1000 else 0.0
        Log.d("QuantityPickerFragment", "Grams: $grams")

        return Triple(quantity.toInt().toDouble(), unit, grams)
    }

    private fun getUnitsForCategory(category: FoodStateEnum): List<String> {
        val units = when (category) {
            FoodStateEnum.SOLID -> listOf(EMPTY_UNIT, "g", "kg")
            FoodStateEnum.LIQUID -> listOf(EMPTY_UNIT, "ml", "l")
            FoodStateEnum.ALL -> listOf(EMPTY_UNIT, "g", "kg", "ml", "l")
            FoodStateEnum.PIECE -> listOf(EMPTY_UNIT)
            else -> listOf(EMPTY_UNIT)
        }
        Log.d("QuantityPickerFragment", "Generated Units for Category $category: $units")
        return units
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

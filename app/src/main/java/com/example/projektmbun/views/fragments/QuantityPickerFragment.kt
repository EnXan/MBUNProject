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
import com.example.projektmbun.utils.Converters
import com.example.projektmbun.utils.enums.UnitsEnum

class QuantityPickerFragment(
    private val listener: (quantity: Double?, unit: UnitsEnum?) -> Unit,
    private val initialQuantityString: String? = null
) : DialogFragment() {

    private var _binding: QuantityPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = QuantityPickerBinding.inflate(layoutInflater)
        val view = binding.root

        val allUnits = getAllUnits()
        val wheelPickerValue = view.findViewById<WheelPicker>(R.id.wheel_picker_value)
        val wheelPickerGrams = view.findViewById<WheelPicker>(R.id.wheel_picker_grams)
        val wheelPickerUnit = view.findViewById<WheelPicker>(R.id.wheel_picker_unit)

        // Parse initial quantity string
        val (initialQuantity, initialUnit, initialGrams) = parseInitialQuantityString(initialQuantityString)

        // Setup Pickers
        setupUnitPicker(wheelPickerUnit, allUnits, initialUnit)
        setupValuePicker(wheelPickerValue, initialUnit, initialQuantity)
        setupGramsPicker(wheelPickerGrams, initialGrams)
        updateGramsPickerVisibility(wheelPickerGrams, initialUnit)

        var previousUnit = initialUnit
        var previousValue = initialQuantity

        // Add listener for unit picker
        wheelPickerUnit.setOnItemSelectedListener { _, data, _ ->
            val selectedUnit = Converters.toUnitEnum(data.toString())
            val newValue = if (previousValue in getRangeForUnit(selectedUnit)) {
                previousValue
            } else {
                getDefaultValueForUnit(selectedUnit)
            }
            setupValuePicker(wheelPickerValue, selectedUnit, newValue)
            updateGramsPickerVisibility(wheelPickerGrams, selectedUnit)

            previousUnit = selectedUnit
            previousValue = newValue
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
                val selectedUnit = Converters.toUnitEnum(wheelPickerUnit.data[wheelPickerUnit.currentItemPosition].toString())
                listener(selectedValue + selectedGrams, if (selectedUnit != UnitsEnum.UNBEKANNT) selectedUnit else null)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private fun getDefaultValueForUnit(unit: UnitsEnum): Double {
        return when (unit) {
            UnitsEnum.GRAMM, UnitsEnum.MILLILITER -> 100.0
            else -> 1.0
        }
    }

    private fun getRangeForUnit(unit: UnitsEnum): ClosedRange<Double> {
        return when (unit) {
            UnitsEnum.GRAMM, UnitsEnum.MILLILITER -> 10.0..1000.0
            UnitsEnum.KILOGRAMM, UnitsEnum.LITER -> 1.0..10.0
            UnitsEnum.STUECK -> 1.0..10.0
            else -> 1.0..5.0
        }
    }

    private fun setupUnitPicker(wheelPickerUnit: WheelPicker, units: List<UnitsEnum>, initialUnit: UnitsEnum) {
        wheelPickerUnit.data = units.map { it.toString() }
        val initialPosition = units.indexOf(initialUnit).takeIf { it >= 0 } ?: 0
        wheelPickerUnit.post { wheelPickerUnit.selectedItemPosition = initialPosition }
    }

    private fun setupValuePicker(wheelPickerValue: WheelPicker, unit: UnitsEnum, initialQuantity: Double) {
        val range = getRangeForUnit(unit)
        val step = 1.0
        val values = generateSequence(range.start) { it + step }
            .takeWhile { it <= range.endInclusive }
            .map { it.toInt().toString() }
            .toList()

        wheelPickerValue.data = values

        // Fallback: Prüfen, ob der initialQuantity-Wert in den verfügbaren Werten enthalten ist
        val position = values.indexOf(initialQuantity.toInt().toString()).takeIf { it >= 0 }
            ?: values.indexOf(getDefaultValueForUnit(unit).toInt().toString())

        // Sicherstellen, dass der WheelPicker richtig aktualisiert wird
        wheelPickerValue.setSelectedItemPosition(position, false) // Ohne Animation
        wheelPickerValue.invalidate() // Forciert ein Redraw
    }




    private fun setupGramsPicker(wheelPickerGrams: WheelPicker, initialGrams: Double) {
        val gramsValues = (0..950 step 50).map { String.format(",%03d", it) }
        wheelPickerGrams.data = gramsValues
        val position = gramsValues.indexOf(String.format(",%03d", initialGrams.toInt()))
        wheelPickerGrams.post { wheelPickerGrams.selectedItemPosition = position.coerceAtLeast(0) }
    }

    private fun updateGramsPickerVisibility(wheelPickerGrams: WheelPicker, unit: UnitsEnum) {
        wheelPickerGrams.visibility = if (unit == UnitsEnum.KILOGRAMM || unit == UnitsEnum.LITER) View.VISIBLE else View.GONE
    }

    private fun parseInitialQuantityString(quantityString: String?): Triple<Double, UnitsEnum, Double> {
        val defaultQuantity = 100.0
        val defaultUnit = UnitsEnum.GRAMM
        val quantityParts = quantityString?.trim()?.split(" ") ?: listOf()
        val quantity = quantityParts.getOrNull(0)?.toDoubleOrNull() ?: defaultQuantity
        val unit = Converters.toUnitEnum(quantityParts.getOrNull(1) ?: defaultUnit.toString())
        val grams = if (unit == UnitsEnum.KILOGRAMM) (quantity - quantity.toInt()) * 1000 else 0.0
        return Triple(quantity.toInt().toDouble(), unit, grams)
    }

    private fun getAllUnits(): List<UnitsEnum> {
        return listOf(UnitsEnum.GRAMM, UnitsEnum.KILOGRAMM, UnitsEnum.MILLILITER, UnitsEnum.LITER, UnitsEnum.STUECK)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.projektmbun.views.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DatePickerFragment(
    private val listener: (year: Int, month: Int, day: Int) -> Unit,
    private val defaultYear: Int,
    private val defaultMonth: Int,
    private val defaultDay: Int
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            listener(selectedYear, selectedMonth, selectedDay)
        }, defaultYear, defaultMonth, defaultDay)

        val calendar = Calendar.getInstance()
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        return datePickerDialog
    }
}

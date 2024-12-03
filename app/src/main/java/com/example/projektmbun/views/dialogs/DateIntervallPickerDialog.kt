package com.example.projektmbun.views.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.projektmbun.R
import java.util.Calendar
import java.util.Date

class DateIntervalPickerDialog(
    context: Context,
    private val onDateIntervalSelected: (Date, String) -> Unit
) : Dialog(context) {

    private lateinit var calendarView: CalendarView
    private lateinit var intervalGroup: RadioGroup
    private lateinit var buttonConfirm: Button
    private lateinit var buttonCancel: Button

    private var selectedDate: Date = Date() // Standardmäßig auf das heutige Datum setzen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_date_intervall_picker)

        // Views initialisieren
        calendarView = findViewById(R.id.calendar_view)
        intervalGroup = findViewById(R.id.interval_group)
        buttonConfirm = findViewById(R.id.button_confirm)
        buttonCancel = findViewById(R.id.button_cancel)

        // Nur zukünftige Daten erlauben
        calendarView.minDate = System.currentTimeMillis()

        // Kalenderauswahl
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        }

        // Buttons
        buttonCancel.setOnClickListener { dismiss() }
        buttonConfirm.setOnClickListener {
            val selectedInterval = when (findViewById<RadioButton>(intervalGroup.checkedRadioButtonId)) {
                findViewById<RadioButton>(R.id.interval_daily) -> "Täglich"
                findViewById<RadioButton>(R.id.interval_weekly) -> "Wöchentlich"
                findViewById<RadioButton>(R.id.interval_2_weeks) -> "Alle 2 Wochen"
                findViewById<RadioButton>(R.id.interval_3_weeks) -> "Alle 3 Wochen"
                findViewById<RadioButton>(R.id.interval_4_weeks) -> "Alle 4 Wochen"
                else -> null
            }

            if (selectedInterval != null) {
                onDateIntervalSelected(selectedDate, selectedInterval)
                dismiss()
            }
        }
    }
}

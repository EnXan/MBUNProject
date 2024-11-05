package com.example.projektmbun.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Collection of converter functions to add support to certain datatypes for SQLite database.
 */
class Converters {

    /**
     * Represents date in format YYYY-MM-DD
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * @param date A nullable LocalDate.
     * @return String representation of date with ISO_LOCAL_DATE pattern.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    /**
     * @param dateString A nullable String of date
     * @return null if String is null
     * @return LocalDate if String is not null
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, formatter) }
    }
}
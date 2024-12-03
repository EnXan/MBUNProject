package com.example.projektmbun.utils

import androidx.room.TypeConverter
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum

/**
 * Utility object for converting between `FoodCategoryEnum` and its textual representation.
 * Includes Room `TypeConverter` methods for database storage and retrieval.
 */
object Converters {

    /**
     * Converts a `FoodCategoryEnum` value to its string representation for database storage.
     * @param value The `FoodCategoryEnum` value to convert.
     * @return The string representation of the enum, or `null` if the input is `null`.
     */
    @TypeConverter
    fun fromCategoryEnum(value: FoodCategoryEnum?): String? {
        return value?.name
    }

    /**
     * Converts a string representation of a `FoodCategoryEnum` back to its enum value.
     * @param value The string representation of the enum.
     * @return The corresponding `FoodCategoryEnum` value, or `null` if the input is `null` or invalid.
     */
    @TypeConverter
    fun toCategoryEnum(value: String?): FoodCategoryEnum? {
        return value?.let {
            try {
                FoodCategoryEnum.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun fromUnitsEnum(value: UnitsEnum): String {
        return value.name
    }

    @TypeConverter
    fun toUnitsEnum(value: String): UnitsEnum {
        return value.let {
            try {
                UnitsEnum.valueOf(it)
            } catch (e: IllegalArgumentException) {
                UnitsEnum.UNITLESS
            }
        }
    }

    /**
     * Converts a human-readable text representation of a category to its corresponding `FoodCategoryEnum` value.
     * @param text The text representation of the category.
     * @return The corresponding `FoodCategoryEnum` value, or `null` if the input does not match any category.
     */
    @JvmStatic
    fun getCategoryEnumFromText(text: String): FoodCategoryEnum? {
        return when (text.lowercase()) {
            "gemüse" -> FoodCategoryEnum.GEMUESE
            "obst" -> FoodCategoryEnum.OBST
            "fleisch" -> FoodCategoryEnum.FLEISCH
            "getreide" -> FoodCategoryEnum.GETREIDE
            "milchprodukt" -> FoodCategoryEnum.MILCHPRODUKT
            "fisch" -> FoodCategoryEnum.FISCH
            "hühlsenfrucht" -> FoodCategoryEnum.HUELSENFRUCHT
            "samen" -> FoodCategoryEnum.SAMEN
            "nuss" -> FoodCategoryEnum.NUSS
            "öl" -> FoodCategoryEnum.OEL
            "fett" -> FoodCategoryEnum.FETT
            "ei" -> FoodCategoryEnum.EI
            "gewürz" -> FoodCategoryEnum.GEWUERZ
            else -> null
        }
    }

    /**
     * Converts a `FoodCategoryEnum` value to its human-readable text representation.
     * @param enum The `FoodCategoryEnum` value to convert.
     * @return The human-readable text representation of the enum, or `null` if the input does not match any category.
     */
    @JvmStatic
    fun getCategoryTextFromEnum(enum: FoodCategoryEnum): String? {
        return when (enum) {
            FoodCategoryEnum.GEMUESE -> "Gemüse"
            FoodCategoryEnum.OBST -> "Obst"
            FoodCategoryEnum.FLEISCH -> "Fleisch"
            FoodCategoryEnum.GETREIDE -> "Getreide"
            FoodCategoryEnum.MILCHPRODUKT -> "Milchprodukt"
            FoodCategoryEnum.FISCH -> "Fisch"
            FoodCategoryEnum.HUELSENFRUCHT -> "Hühlsenfrucht"
            FoodCategoryEnum.SAMEN -> "Samen"
            FoodCategoryEnum.NUSS -> "Nuss"
            FoodCategoryEnum.OEL -> "Öl"
            FoodCategoryEnum.FETT -> "Fett"
            FoodCategoryEnum.EI -> "Ei"
            FoodCategoryEnum.GEWUERZ -> "Gewürz"
            else -> null
        }
    }

    @JvmStatic
    fun getUnitEnumFromText(text: String): UnitsEnum {
        return when (text.lowercase()) {
            "g" -> UnitsEnum.GRAMM
            "stueck" -> UnitsEnum.STUECK
            "kg" -> UnitsEnum.KILOGRAMM
            "l" -> UnitsEnum.LITER
            "ml" -> UnitsEnum.MILLILITER
            "tl" -> UnitsEnum.TEELOEFEL
            "el" -> UnitsEnum.ESSLOEFEL
            "prise" -> UnitsEnum.PRISE
            "tasse" -> UnitsEnum.TASSE
            "glas" -> UnitsEnum.GLAS
            "packung" -> UnitsEnum.PACKUNG
            "nach geschmack" -> UnitsEnum.NACH_GESCHMACK
            "dose" -> UnitsEnum.DOSE
            "becher" -> UnitsEnum.BECHER
            else -> UnitsEnum.STUECK

        }
    }

    @JvmStatic
    fun getUnitTextFromEnum(enum: UnitsEnum): String {
        return when (enum) {
            UnitsEnum.GRAMM -> "g"
            UnitsEnum.STUECK -> "\u00A0"
            UnitsEnum.KILOGRAMM -> "kg"
            UnitsEnum.LITER -> "l"
            UnitsEnum.MILLILITER -> "ml"
            UnitsEnum.TEELOEFEL -> "TL"
            UnitsEnum.ESSLOEFEL -> "EL"
            UnitsEnum.PRISE -> "Prise"
            UnitsEnum.TASSE -> "Tasse"
            UnitsEnum.GLAS -> "Glas"
            UnitsEnum.PACKUNG -> "Packung"
            UnitsEnum.NACH_GESCHMACK -> "Nach Geschmack"
            UnitsEnum.DOSE -> "Dose"
            UnitsEnum.BECHER -> "Becher"
            UnitsEnum.UNITLESS -> "\u00A0"
        }
    }
}

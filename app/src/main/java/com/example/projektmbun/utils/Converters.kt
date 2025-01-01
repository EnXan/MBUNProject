package com.example.projektmbun.utils

import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum

/**
 * Utility object for converting between `FoodCategoryEnum` and its textual representation.
 * Includes Room `TypeConverter` methods for database storage and retrieval.
 */
object Converters {
    /**
     * Converts a human-readable text representation of a category to its corresponding `FoodCategoryEnum` value.
     * @param text The text representation of the category.
     * @return The corresponding `FoodCategoryEnum` value, or `null` if the input does not match any category.
     */
    @JvmStatic
    fun toCategoryEnum(text: String): FoodCategoryEnum? {
        return when (text.lowercase()) {
            "gemüse" -> FoodCategoryEnum.GEMUESE
            "obst" -> FoodCategoryEnum.OBST
            "fleisch" -> FoodCategoryEnum.FLEISCH
            "getreide" -> FoodCategoryEnum.GETREIDE
            "milchprodukt" -> FoodCategoryEnum.MILCHPRODUKT
            "fisch" -> FoodCategoryEnum.FISCH
            "soße" -> FoodCategoryEnum.SOSSE
            "getränk" -> FoodCategoryEnum.GETRAENK
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
    fun fromCategoryEnum(enum: FoodCategoryEnum): String {
        return when (enum) {
            FoodCategoryEnum.GEMUESE -> "Gemüse"
            FoodCategoryEnum.OBST -> "Obst"
            FoodCategoryEnum.FLEISCH -> "Fleisch"
            FoodCategoryEnum.GETREIDE -> "Getreide"
            FoodCategoryEnum.MILCHPRODUKT -> "Milchprodukt"
            FoodCategoryEnum.FISCH -> "Fisch"
            FoodCategoryEnum.HUELSENFRUCHT -> "Hühlsenfrucht"
            FoodCategoryEnum.SAMEN -> "Samen"
            FoodCategoryEnum.SOSSE -> "Soße"
            FoodCategoryEnum.GETRAENK -> "Getränk"
            FoodCategoryEnum.NUSS -> "Nuss"
            FoodCategoryEnum.OEL -> "Öl"
            FoodCategoryEnum.FETT -> "Fett"
            FoodCategoryEnum.EI -> "Ei"
            FoodCategoryEnum.GEWUERZ -> "Gewürz"
            FoodCategoryEnum.UNBEKANNT -> "Unbekannt"
        }
    }

    @JvmStatic
    fun toUnitEnum(text: String?): UnitsEnum {
        return when (text?.lowercase()) {
            "g", "gramm" -> UnitsEnum.GRAMM
            "stück", "stueck" -> UnitsEnum.STUECK
            "kg", "kilogramm" -> UnitsEnum.KILOGRAMM
            "l", "liter" -> UnitsEnum.LITER
            "ml", "milliliter" -> UnitsEnum.MILLILITER
            "tl", "teeloeffel" -> UnitsEnum.TEELOEFEL
            "el", "essloefel" -> UnitsEnum.ESSLOEFEL
            "prise" -> UnitsEnum.PRISE
            "zehe" -> UnitsEnum.ZEHE
            "tasse" -> UnitsEnum.TASSE
            "glas" -> UnitsEnum.GLAS
            "packung" -> UnitsEnum.PACKUNG
            "nach geschmack", "nach_geschmack" -> UnitsEnum.NACH_GESCHMACK
            "dose" -> UnitsEnum.DOSE
            "becher" -> UnitsEnum.BECHER
            else -> UnitsEnum.UNBEKANNT
        }
    }

    @JvmStatic
    fun fromUnitEnum(enum: UnitsEnum): String {
        return when (enum) {
            UnitsEnum.GRAMM -> "g"
            UnitsEnum.STUECK -> "Stück"
            UnitsEnum.KILOGRAMM -> "kg"
            UnitsEnum.LITER -> "l"
            UnitsEnum.MILLILITER -> "ml"
            UnitsEnum.TEELOEFEL -> "TL"
            UnitsEnum.ESSLOEFEL -> "EL"
            UnitsEnum.ZEHE -> "Zehe"
            UnitsEnum.PRISE -> "Prise"
            UnitsEnum.TASSE -> "Tasse"
            UnitsEnum.GLAS -> "Glas"
            UnitsEnum.PACKUNG -> "Packung"
            UnitsEnum.NACH_GESCHMACK -> "Nach Geschmack"
            UnitsEnum.DOSE -> "Dose"
            UnitsEnum.BECHER -> "Becher"
            UnitsEnum.UNBEKANNT -> "Unbekannte Einheit"
        }
    }
}

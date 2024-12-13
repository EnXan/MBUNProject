package com.example.projektmbun.utils.enums

import kotlinx.serialization.Serializable

/**
 * Enum of all possible food categories.
 */
@Serializable
enum class FoodCategoryEnum {
    GEMUESE,
    OBST,
    FLEISCH,
    GETREIDE,
    MILCHPRODUKT,
    FISCH,
    HUELSENFRUCHT,
    SAMEN,
    NUSS,
    OEL,
    FETT,
    EI,
    GEWUERZ,
    UNBEKANNT
}
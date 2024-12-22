package com.example.projektmbun.utils.enums

import kotlinx.serialization.Serializable

/**
 * Enum of all possible food categories.
 */
@Serializable
enum class FoodCategoryEnum {
    MILCHPRODUKT,
    FLEISCH,
    GETREIDE,
    GEMUESE,
    OBST,
    SOSSE,
    FISCH,
    GETRAENK,
    EI,
    HUELSENFRUCHT,
    SAMEN,
    NUSS,
    OEL,
    FETT,
    GEWUERZ,
    UNBEKANNT
}
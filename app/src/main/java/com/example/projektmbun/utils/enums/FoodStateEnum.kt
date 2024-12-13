package com.example.projektmbun.utils.enums

import kotlinx.serialization.Serializable

/**
 * Enum of all possible food states.
 */
@Serializable
enum class FoodStateEnum {
    SOLID,
    LIQUID,
    ALL,
    PIECE,
    UNBEKANNT
}
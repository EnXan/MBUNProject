package com.example.projektmbun.utils.enums

import kotlinx.serialization.Serializable

/**
 * An enum class representing the state of a food card depending on it's storage location.
 */
@Serializable
enum class FoodCardStateEnum {
    TEMPORARY,
    PERMANENT,
    BOTH
}
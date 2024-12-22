package com.example.projektmbun.models.data_structure.food_card

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projektmbun.models.data_structure.food.FoodLocal

data class FoodCardWithDetails(
    @Embedded val foodCard: FoodCard,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "name" // Food.name als Fremdschlüssel
    )
    val foodLocal: FoodLocal
)
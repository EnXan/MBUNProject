package com.example.projektmbun.models.data_structure.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.routine.Routine
import com.example.projektmbun.models.data_structure.stock.Stock

/**
 * Represents a routine with its associated food cards.
 * Combines the `Routine` entity with a list of related `FoodCard` entities.
 *
 * @property routine The routine details from the `Routine` entity.
 * @property foodCards A list of food cards associated with the routine, linked by the `routineId` property.
 */
data class RoutineWithFoodCards(
    @Embedded val routine: Routine,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val foodCards: List<FoodCard>
)

/**
 * Represents a stock with its associated food cards.
 * Combines the `Stock` entity with a list of related `FoodCard` entities.
 *
 * @property stock The stock details from the `Stock` entity.
 * @property foodCards A list of food cards associated with the stock, linked by the `stockId` property.
 */
data class StockWithFoodCards(
    @Embedded val stock: Stock,
    @Relation(
        parentColumn = "id",
        entityColumn = "stockId"
    )
    val foodCards: List<FoodCard>
)

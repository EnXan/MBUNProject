package com.example.projektmbun.models.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.models.data.food_card.FoodCard
import com.example.projektmbun.models.data.routine.Routine
import com.example.projektmbun.models.data.stock.Stock

/**
 * Represents a food card with its associated food details.
 * Combines the `FoodCard` entity with its related `Food` entity.
 *
 * @property foodCard The food card details from the `FoodCard` entity.
 * @property food The associated food details from the `Food` entity, linked by the `foodId` property.
 */
data class FoodCardWithDetails(
    @Embedded val foodCard: FoodCard,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "name"
    )
    val food: Food,
)

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

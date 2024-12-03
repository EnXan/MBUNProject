package com.example.projektmbun.models.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.projektmbun.models.data.relations.StockWithFoodCards

/**
 * Data Access Object (DAO) interface for performing database operations on the `Stock` entity
 * along with its related food cards. Provides methods to retrieve a stock with its associated
 * food cards by stock ID.
 */
@Dao
interface StockWithFoodCardsDao {

    /**
     * Get a stock with its food cards by its id.
     * @param stockId the id of the stock to get.
     * @return a StockWithFoodCards containing the stock and its food cards `StockWithFoodCards` or null if not found `null`.
     */
    @Transaction
    @Query("""
        SELECT *
        FROM Stock
        WHERE id = :stockId
    """)
    suspend fun getStockWithFoodCards(stockId: Int): StockWithFoodCards?
}
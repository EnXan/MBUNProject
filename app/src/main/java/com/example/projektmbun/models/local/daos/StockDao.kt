package com.example.projektmbun.models.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projektmbun.models.data_structure.stock.Stock

/**
 * Data Access Object (DAO) interface for performing database operations on the `Stock` entity.
 * Provides methods for creating a new stock and retrieving a stock ID by its name.
 */
@Dao
interface StockDao {

    /**
     * Create a new stock.
     * @param stock the stock to create.
     * @return the id of the created stock `Long`.
     */
    @Insert
    suspend fun createStock(stock: Stock): Long

    /**
     * Get the id of a stock by its name.
     * @param stockName the name of the stock.
     * @return the id of the stock `Int`.
     */
    @Query("SELECT id FROM stock WHERE name = :stockName")
    suspend fun getStockIdByName(stockName: String): Int

}
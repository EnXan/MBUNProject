package com.example.projektmbun.controller

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.util.Log
import com.example.projektmbun.models.local.daos.FoodCardDao
import com.example.projektmbun.models.local.daos.StockDao
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.utils.enums.FoodCardStateEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Controller class responsible for managing operations related to the stock,
 * including adding food cards to the stock and retrieving food cards from the stock.
 *
 * @property stockDao DAO for performing stock-related operations.
 * @property foodCardDao DAO for performing food card operations.
 */
class StockController(private val stockDao: StockDao, private val foodCardDao: FoodCardDao) {

    /**
     * getFoodCardsInStock
     */


    /**
     * Methode zum Hinzufügen einer FoodCard zur StockList.
     * @param foodCard die FoodCard die hinzugefügt werden soll.
     * @throws SQLiteConstraintException wenn die FoodCard bereits in der Datenbank existiert.
     * @throws SQLiteException wenn ein SQL-Fehler auftritt.
     * @throws Exception wenn ein anderer Fehler auftritt.
     */
    suspend fun addFoodCardToStock(foodCard: FoodCard, stockId: Int?): Long? {

        val updatedFoodCard = foodCard.copy(isActive = true, stockId = stockId, state = FoodCardStateEnum.PERMANENT)

        return withContext(Dispatchers.IO) {
            try {
                foodCardDao.insertFoodCard(updatedFoodCard)
            } catch (e: SQLiteConstraintException) {
                Log.e("UpdateError", "SQLiteConstraintException: ${e.message}. FoodCard: $updatedFoodCard, StockID: $stockId")
                null
            } catch (e: SQLiteException) {
                Log.e("UpdateError", "SQLiteConstraintException: ${e.message}. FoodCard: $updatedFoodCard, StockID: $stockId")
                null
            } catch (e: Exception) {
                Log.e("UpdateError", "SQLiteConstraintException: ${e.message}. FoodCard: $updatedFoodCard, StockID: $stockId")
                null
            }
        }
    }

}
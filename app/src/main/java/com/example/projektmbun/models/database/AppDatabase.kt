package com.example.projektmbun.models.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.QueryCallback
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projektmbun.models.daos.FoodCardDao
import com.example.projektmbun.models.daos.FoodCardWithDetailsDao
import com.example.projektmbun.models.daos.FoodDao
import com.example.projektmbun.models.daos.RecipeDao
import com.example.projektmbun.models.daos.RoutineDao
import com.example.projektmbun.models.daos.RoutineWithFoodCardsDao
import com.example.projektmbun.models.daos.StockDao
import com.example.projektmbun.models.daos.StockWithFoodCardsDao
import com.example.projektmbun.models.data.food.Food
import com.example.projektmbun.models.data.food_card.FoodCard
import com.example.projektmbun.models.data.recipe.DishTypes
import com.example.projektmbun.models.data.recipe.Equipment
import com.example.projektmbun.models.data.recipe.Ingredient
import com.example.projektmbun.models.data.recipe.Instructions
import com.example.projektmbun.models.data.recipe.Recipe
import com.example.projektmbun.models.data.routine.Routine
import com.example.projektmbun.models.data.stock.Stock
import com.example.projektmbun.utils.Converters
import java.util.concurrent.Executors


/**
 * Represents the main database for the application.
 * Manages entities, DAOs, and database configuration.
 * Provides a singleton instance of the database for use throughout the application.
 */
@Database(
    entities = [
        Food::class, FoodCard::class, Recipe::class, DishTypes::class, Equipment::class,
        Ingredient::class, Instructions::class, Routine::class, Stock::class
    ],
    version = 37
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Accessor for the `FoodDao` to perform operations on the `Food` entity.
     */
    abstract fun foodDao(): FoodDao

    /**
     * Accessor for the `FoodCardDao` to perform operations on the `FoodCard` entity.
     */
    abstract fun foodCardDao(): FoodCardDao

    /**
     * Accessor for the `RecipeDao` to perform operations on the `Recipe` entity.
     */
    abstract fun recipeDao(): RecipeDao

    /**
     * Accessor for the `RoutineDao` to perform operations on the `Routine` entity.
     */
    abstract fun routineDao(): RoutineDao

    /**
     * Accessor for the `StockDao` to perform operations on the `Stock` entity.
     */
    abstract fun stockDao(): StockDao

    /**
     * Accessor for the `FoodCardWithDetailsDao` to retrieve food cards with their associated details.
     */
    abstract fun foodCardWithDetailsDao(): FoodCardWithDetailsDao

    /**
     * Accessor for the `RoutineWithFoodCardsDao` to retrieve routines with their associated food cards.
     */
    abstract fun routineWithFoodCardsDao(): RoutineWithFoodCardsDao

    /**
     * Accessor for the `StockWithFoodCardsDao` to retrieve stocks with their associated food cards.
     */
    abstract fun stockWithFoodCardsDao(): StockWithFoodCardsDao

    /**
     * Singleton instance of the database.
     * Ensures only one instance of the database is created across the application.
     */
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Checks for foreign key constraint violations in the database and logs any failures.
         * @param database The SQLite database to check.
         */
        fun checkForeignKeyConstraints(database: SupportSQLiteDatabase) {
            val cursor = database.query("PRAGMA foreign_key_check")
            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                val rowId = cursor.getInt(1)
                val parentTable = cursor.getString(2)
                Log.e(
                    "ForeignKeyCheck",
                    "Constraint failed in table $tableName for row $rowId referencing $parentTable"
                )
            }
            cursor.close()
        }

        /**
         * Retrieves the singleton instance of the database, creating it if necessary.
         * @param context The application context.
         * @return The singleton instance of `AppDatabase`.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appDB"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            checkForeignKeyConstraints(db)
                        }
                    })
                    .setQueryCallback(QueryCallback { sqlQuery, bindArgs ->
                        Log.d("DB_QUERY", "SQL Query: $sqlQuery, Args: $bindArgs")
                    }, Executors.newSingleThreadExecutor())
                    //.fallbackToDestructiveMigration()
                    .createFromAsset("database/appDB.db") // Use a predefined database
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

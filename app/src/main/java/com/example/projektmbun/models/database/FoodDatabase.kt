package com.example.projektmbun.models.database

import com.example.projektmbun.utils.Converters
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.projektmbun.models.daos.FoodCardDao
import com.example.projektmbun.models.daos.FoodDao
import com.example.projektmbun.models.data.Food
import com.example.projektmbun.models.data.FoodCard


@Database(
    entities = [Food::class, FoodCard::class],
    version = 2
)

@TypeConverters(Converters::class)
abstract class FoodDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun foodCardDao(): FoodCardDao


    /**
     * Singleton-Instance of database
     */
    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "food_database"
                )
                    .createFromAsset("database/food_database.db") //Use predefined database
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
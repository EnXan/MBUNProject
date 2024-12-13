package com.example.projektmbun.models.data_structure.stock

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a stock in the database, used to manage and organize food items in storage.
 *
 * @property id The unique identifier of the stock (auto-generated primary key).
 * @property name The name of the stock, used to identify or categorize the storage location.
 */
@Entity(
    tableName = "stock"
)
data class Stock(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String
)

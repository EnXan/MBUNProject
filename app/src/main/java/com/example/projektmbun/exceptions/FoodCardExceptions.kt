package com.example.projektmbun.exceptions

/**
 * Exception thrown when there is an error during the creation of a food card.
 *
 * @param message The error message describing the issue.
 */
class FoodCardCreationException(message: String) : Exception(message)

/**
 * Exception thrown when there is an error during the deletion of a food card.
 *
 * @param message The error message describing the issue.
 */
class FoodCardDeletionException(message: String) : Exception(message)

/**
 * Exception thrown when a food card is not found in the database.
 *
 * @param message The error message describing the issue.
 */
class FoodCardNotFoundException(message: String) : Exception(message)

/**
 * Exception thrown when there is an error during the update of a food card.
 *
 * @param message The error message describing the issue.
 */
class FoodCardUpdateException(message: String) : Exception(message)

/**
 * Exception thrown when there is an error related to the stock operations for a food card.
 *
 * @param message The error message describing the issue.
 */
class FoodCardStockException(message: String) : Exception(message)

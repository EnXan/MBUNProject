package com.example.projektmbun.exceptions

/**
 * Exception thrown when a recipe is not found in the database or data source.
 *
 * @param message The error message describing the issue.
 */
class RecipeNotFoundException(message: String) : Exception(message)

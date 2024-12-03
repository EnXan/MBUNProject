package com.example.projektmbun.exceptions

/**
 * Exception thrown when a routine update operation fails, such as when no rows are updated in the database.
 *
 * @param message The error message describing the issue.
 */
class RoutineNotUpdatedException(message: String) : Exception(message)

/**
 * Exception thrown when a routine deletion operation fails, such as when no rows are deleted in the database
 * or a database constraint prevents the deletion.
 *
 * @param message The error message describing the issue.
 */
class RoutineDeletionException(message: String) : Exception(message)

package com.example.projektmbun.utils

import org.apache.commons.text.similarity.LevenshteinDistance

/**
 * Utility object for performing fuzzy search operations with prioritization for exact matches and substring matches.
 * Utilizes Levenshtein distance for similarity scoring.
 */
object SearchLogic {
    private val levenshteinDistance = LevenshteinDistance()

    /**
     * Performs a fuzzy search on a list of items, prioritizing exact matches and substring matches.
     * Items are scored based on their similarity to the query and are filtered and sorted by relevance.
     *
     * @param query The search query entered by the user.
     * @param items The list of items to search through. Each item must have a `name` or searchable property.
     * @param nameSelector A function to extract the name or searchable string from each item.
     * @param threshold The minimum relevance score required to include an item in the results (default is -3).
     * @return A list of items sorted by their relevance to the query.
     *
     * Example usage:
     * ```
     * val items = listOf("apple", "banana", "grape", "pineapple")
     * val results = SearchLogic.fuzzySearch("app", items, { it }, threshold = -5)
     * ```
     */
    fun <T> fuzzySearch(
        query: String,
        items: List<T>,
        nameSelector: (T) -> String,
        threshold: Int = -3
    ): List<T> {
        if (query.isBlank()) return emptyList()

        return items.map { item ->
            val itemName = nameSelector(item).lowercase()
            val similarityScore = getRelevanceScore(query.lowercase(), itemName)
            item to similarityScore
        }
            .filter { it.second >= threshold } // Exclude items below the threshold
            .sortedByDescending { it.second } // Sort by relevance in descending order
            .map { it.first } // Return the original items
    }

    /**
     * Computes a relevance score for a target string based on its similarity to the query.
     * The score incorporates the following factors:
     * - **Levenshtein Distance**: A penalty proportional to the edit distance between the query and the target.
     * - **Exact Match Boost**: A fixed bonus for exact matches (default +100).
     * - **Substring Match Boost**: A fixed bonus for substring matches (default +50).
     *
     * @param query The search query string.
     * @param target The target string to compare against the query.
     * @return The calculated relevance score, where higher scores indicate closer matches.
     */
    private fun getRelevanceScore(query: String, target: String): Int {
        val levenshteinScore = levenshteinDistance.apply(query, target)
        val exactMatchBoost = if (query == target) 100 else 0
        val substringBoost = if (target.contains(query)) 50 else 0
        return -levenshteinScore + exactMatchBoost + substringBoost
    }
}

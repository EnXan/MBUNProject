package com.example.projektmbun.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * Extension function for `TextView` to add a search listener that triggers a callback whenever the text changes.
 * Designed for asynchronous operations using a `CoroutineScope`.
 *
 * @param scope The `CoroutineScope` in which the callback will be executed. Typically, this could be a `LifecycleCoroutineScope`.
 * @param callback A lambda function to be invoked with the updated search query as a `String`.
 *
 * Example usage:
 * ```
 * searchTextView.addSearchListener(lifecycleScope) { query ->
 *     // Perform search or update UI based on the query
 * }
 * ```
 */
fun TextView.addSearchListener(scope: CoroutineScope, callback: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val query = s?.toString() ?: ""
            scope.launch {
                callback(query)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

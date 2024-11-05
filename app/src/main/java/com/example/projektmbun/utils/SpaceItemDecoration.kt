package com.example.projektmbun.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Contains a function to get vertical space between items in a RecyclerView.
 * @param verticalSpaceHeight representing the vertical space
 */
class SpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    /**
     * Overwrites RecyclerView.ItemDecoration allowing to define offsets for item's boundaries.
     * @param outRect A Rect that defines the offset (space) for each side of the item view
     * (in this case only the bottom offset).
     * @param view The individual item view within the RecyclerView.
     * @param parent The RecyclerView itself
     * @param state Provides Information about the current state of RecyclerView.
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = verticalSpaceHeight
    }
}

package com.amarant.apps.budgetapp.ui.adapter.decorations

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter

class CustomDividerDecoration(
    private val divider: Drawable
) : RecyclerView.ItemDecoration() {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val holder = parent.getChildViewHolder(child)
            if (holder.itemViewType == ReportsAdapter.VIEW_TYPE_DATE) continue
            val position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) continue
            val nextPosition = position + 1
            val adapter = parent.adapter ?: continue
            if (nextPosition < adapter.itemCount) {
                val nextViewType = adapter.getItemViewType(nextPosition)
                if (nextViewType == ReportsAdapter.VIEW_TYPE_DATE) {
                    continue
                }
            } else {
                continue
            }
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }
}

package com.ahu.ahutong.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.ahu.ahutong.R
import kotlin.math.abs


class HeaderSearchBehavior(val context: Context, val attr: AttributeSet) :
    CoordinatorLayout.Behavior<View>(
        context,
        attr
    ) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (dependency.id == R.id.image) {
            return true
        }
        return false
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (dependency.id == R.id.image) {
            val resources = dependency.resources
            val header_height = resources.getDimension(R.dimen.collapsed_header_height)
            val offset_y = resources.getDimension(R.dimen.collapsed_float_offset_y)
            val child_height = child.height
            //获取当前控件
            val child_height_end = (header_height - child_height) / 2 - (header_height + offset_y)
            //获取进度
            val progress = 1f - abs(dependency.translationY / (dependency.height - header_height))
            //为搜索框设置初始位置
            child.translationY = header_height + offset_y
            //通过marigin改变搜索框位置
            val collapsedMargin = resources.getDimension(R.dimen.collapsed_search_margin_right)
            val zero = resources.getDimension(R.dimen.collapsed_float_zero)
            val marginLeftRight = resources.getDimension(R.dimen.collapsed_search_margin_left2right)
            val marginRight =
                (collapsedMargin + (marginLeftRight - collapsedMargin) * progress).toInt()
            val marginTop = (child_height_end + (zero - child_height_end) * progress).toInt()
            val marginLeft = marginLeftRight.toInt()
            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            lp.setMargins(marginLeft, marginTop, marginRight, 0)
            child.layoutParams = lp
            return true
        }
        return false
    }

}
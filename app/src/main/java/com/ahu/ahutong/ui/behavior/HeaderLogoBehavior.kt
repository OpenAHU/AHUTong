package com.ahu.ahutong.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.ahu.ahutong.R
import kotlin.math.abs

class HeaderLogoBehavior (val context: Context, val attr: AttributeSet): CoordinatorLayout.Behavior<View>(
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
        if (dependency.id == R.id.image){
            val resources = dependency.resources
            val header_height = resources.getDimension(R.dimen.collapsed_header_height)
            val progress = 1f - abs(dependency.translationY / (dependency.height - header_height))
            val text = child.findViewById<TextView>(R.id.toolbar_tv)
            text.alpha = progress
            return true
        }
        return false
    }

}
package com.ahu.ahutong.ext

import android.view.MenuItem

import androidx.databinding.BindingAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:04
 * @Email 468766131@qq.com
 */
@BindingAdapter("onSelectItem")
fun onSelectItem(view: BottomNavigationView, action: ((MenuItem) -> Boolean)?) {
    view.setOnNavigationItemSelectedListener(action)
}
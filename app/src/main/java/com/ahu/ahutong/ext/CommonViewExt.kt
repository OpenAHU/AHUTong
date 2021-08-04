package com.ahu.ahutong.ext

import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午1:21
 * @Email: 468766131@qq.com
 */

/**
 * loadImg
 * @param url 图片链接
 */
@BindingAdapter("loadImg")
fun loadImg(view: ShapeableImageView, url: String?) {
    view.isVisible = !url.isNullOrEmpty()
    if (!url.isNullOrEmpty())
        view.load(url)
}
@BindingAdapter("loadImg")
fun loadImg(view: ImageView, id: Int){
    if (id != 0){
        view.load(id)
    }

}

@BindingAdapter("onSelectItem")
fun onSelectItem(view: BottomNavigationView, action: ((MenuItem) -> Boolean)?) {
    view.setOnNavigationItemSelectedListener(action)
}

@BindingAdapter("onIconClick")
fun onIconClick(view: MaterialToolbar, action: (() -> Unit)?){
    view.setNavigationOnClickListener{
        action?.invoke()
    }
}
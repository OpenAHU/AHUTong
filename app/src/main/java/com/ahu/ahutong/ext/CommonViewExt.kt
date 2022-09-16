package com.ahu.ahutong.ext

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
import com.ahu.ahutong.ui.widget.AutoSizeTextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.simon.library.view.NumberPickerView

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
    if (!url.isNullOrEmpty()) {
        view.load(url)
    }
}

@BindingAdapter("loadColor")
fun loadColor(view: ShapeableImageView, color: String?) {
    if (!color.isNullOrEmpty()) {
        view.setImageDrawable(ColorDrawable(Color.parseColor(color)))
    }
}

@BindingAdapter("loadImg")
fun loadImg(view: ImageView, id: Int) {
    if (id != 0) {
        view.load(id)
    }
}

@BindingAdapter("onSelectItem")
fun onSelectItem(view: BottomNavigationView, action: ((MenuItem) -> Boolean)?) {
    view.setOnNavigationItemSelectedListener(action)
}

@BindingAdapter("onIconClick")
fun onIconClick(view: MaterialToolbar, action: (() -> Unit)?) {
    view.setNavigationOnClickListener {
        action?.invoke()
    }
}

@BindingAdapter("autoSizeText")
fun autoSizeText(view: AutoSizeTextView, text: CharSequence) {
    view.setAutoSizeText(text)
}

fun NumberPickerView.defaultStyle() {
    setDividerColor(Color.GRAY)
    setDividerHeight(1f)
    setDividerPadding(35f)
}

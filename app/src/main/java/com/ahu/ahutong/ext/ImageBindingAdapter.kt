package com.ahu.ahutong.ext

import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
import com.google.android.material.imageview.ShapeableImageView

/**
 * @Author SinkDev
 * @Date 2021/7/27-16:55
 * @Email 468766131@qq.com
 */

/**
 *
 * @param url 图片链接
 *
 */
@BindingAdapter("loadImg")
fun loadImg(view: ShapeableImageView, url: String?) {
    view.isVisible = !url.isNullOrEmpty()
    if (!url.isNullOrEmpty())
        view.load(url)
}
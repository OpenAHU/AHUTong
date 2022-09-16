package com.ahu.ahutong.ext

import android.content.res.Resources
import android.util.TypedValue

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午2:21
 * @Email: 468766131@qq.com
 */

val Number.dp
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)

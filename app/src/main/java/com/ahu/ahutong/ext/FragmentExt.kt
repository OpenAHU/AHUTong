package com.ahu.ahutong.ext

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午7:35
 * @Email: 468766131@qq.com
 */

fun Fragment.buildDialog(
    title: String, message: String, positiveText: String,
    positiveListener: DialogInterface.OnClickListener? = null,
    negativeText: String? = null,
    negativeListener: DialogInterface.OnClickListener? = null,
): AlertDialog.Builder {
    val dialog = AlertDialog.Builder(requireActivity())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText, positiveListener)
    negativeText?.let {
        dialog.setNegativeButton(negativeText, negativeListener)
    }
    return dialog
}
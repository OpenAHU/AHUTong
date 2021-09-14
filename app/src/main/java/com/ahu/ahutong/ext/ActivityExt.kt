package com.ahu.ahutong.ext

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

fun Activity.buildDialog(
    title: String, message: String, positiveText: String,
    positiveListener: DialogInterface.OnClickListener? = null,
    negativeText: String? = null,
    negativeListener: DialogInterface.OnClickListener? = null,
): AlertDialog.Builder {
    val dialog = AlertDialog.Builder(requireNotNull(this))
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText, positiveListener)
    negativeText?.let {
        dialog.setNegativeButton(negativeText, negativeListener)
    }
    return dialog
}
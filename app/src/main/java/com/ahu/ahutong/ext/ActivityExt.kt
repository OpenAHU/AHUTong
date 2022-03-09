package com.ahu.ahutong.ext

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.simon.library.view.LoadingDialog

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

fun Activity.buildProgressDialog(
    message: String
): LoadingDialog {
    val loadingDialog = LoadingDialog(this)
    loadingDialog.setMessage(message)
    return loadingDialog
}
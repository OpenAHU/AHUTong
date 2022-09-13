package com.ahu.ahutong.ext

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.simon.library.view.LoadingDialog


fun Activity.buildProgressDialog(
    message: String
): LoadingDialog {
    val loadingDialog = LoadingDialog(this)
    loadingDialog.setMessage(message)
    return loadingDialog
}
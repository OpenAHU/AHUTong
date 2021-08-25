package com.ahu.ahutong.ui.dialog

import android.app.Dialog
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import com.ahu.ahutong.databinding.DialogSettingBinding
import com.ahu.ahutong.ext.dp
import com.simon.library.DialogUtil
import com.simon.library.view.BottomDialog

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午6:38
 * @Email: 468766131@qq.com
 */
class SettingScheduleDialog : BottomDialog() {
    private var callback: SettingCallback? = null

    override fun onInitDialog(dialog: Dialog?) {
        val binding = DialogSettingBinding.inflate(layoutInflater)
        dialog?.setContentView(binding.root)
        binding.proxy = ClickProxy()
        cancelBackKey(dialog)
    }

    fun setCallback(callback: SettingCallback) {
        this.callback = callback
    }

    inner class ClickProxy {
        fun inputSchedule() {
            callback?.inputSchedule()
            dismiss()
        }

        fun addCourse() {
            callback?.addCourse()
            dismiss()

        }

        fun setStartTime() {
            callback?.setStartTime()
            dismiss()
        }

        fun gotoSetting() {
            callback?.gotoSetting()
            dismiss()
        }

        fun close() {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
    }

    interface SettingCallback {
        fun addCourse()
        fun setStartTime()
        fun inputSchedule()
        fun gotoSetting()

    }

}
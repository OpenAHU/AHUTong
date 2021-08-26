package com.ahu.ahutong.ui.dialog

import android.app.Dialog
import android.view.View
import com.ahu.ahutong.databinding.DialogChooseOneBinding
import com.ahu.ahutong.ext.defaultStyle
import com.simon.library.view.BottomDialog

/**
 * @Author: SinkDev
 * @Date: 2021/8/26-下午4:16
 * @Email: 468766131@qq.com
 */
class ChooseOneDialog(val list: List<String>) : BottomDialog() {

    private lateinit var binding: DialogChooseOneBinding
    var selectListener: ((index: Int, data: String) -> Unit)? = null

    override fun onInitDialog(dialog: Dialog?) {
        dialog?.setCanceledOnTouchOutside(false)
        cancelBackKey(dialog)
        binding = DialogChooseOneBinding.inflate(layoutInflater)
        dialog?.setContentView(binding.root)
        binding.apply {
            picker.setDividerMarginL(25)
            picker.setDividerMarginR(25)
            picker.defaultStyle()
            picker.setDisplayedValuesAndPickedIndex(
                list.toTypedArray(), 0, false
            )
            proxy = ClickProxy()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        selectListener = null
        binding.unbind()
    }


    inner class ClickProxy {
        fun cancel(view: View?) {
            dismiss()
        }

        fun ok(view: View?) {
            selectListener?.invoke(binding.picker.value, binding.picker.contentByCurrValue)
            dismiss()
        }
    }
}
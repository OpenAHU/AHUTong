package com.ahu.ahutong.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.DialogRoomBinding
import com.ahu.ahutong.ext.defaultStyle
import com.ahu.ahutong.ui.page.state.ClassRoomViewModel
import com.simon.library.view.BottomDialog

/**
 *
 * @Author: Simon
 * @Date: 2021/8/7-上午12:39
 * @Email: 330771794@qq.com
 */
class ClassRoomDialog(private val campusIndex: Int, private val timesIndex: Int) : BottomDialog() {
    private lateinit var binding: DialogRoomBinding
    private var callBack: CallBack? = null
    override fun onInitDialog(dialog: Dialog) {
        binding =
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_room, null, false)
        dialog.setContentView(binding.root)
        binding.run {
            pickerCampus.setDividerMarginL(25)
            pickerTime.setDividerMarginR(25)
            pickerCampus.defaultStyle()
            pickerTime.defaultStyle()
            pickerCampus.setDisplayedValuesAndPickedIndex(
                ClassRoomViewModel.campus.toTypedArray(),
                campusIndex,
                false
            )
            pickerTime.setDisplayedValuesAndPickedIndex(
                ClassRoomViewModel.times.toTypedArray(),
                timesIndex,
                false
            )
            proxy = ClickProxy()
        }

    }

    fun setCallBack(callBack: CallBack) {
        this.callBack = callBack
    }

    interface CallBack {
        fun dialogCallBack(campus: String, time: String)
    }

    override fun dismiss() {
        //这里需要把callback制空（不然会内存泄漏），本人刚学kt，不会制空，等以后再修改
        super.dismiss()
        callBack = null
        binding.unbind()
    }

    inner class ClickProxy {
        fun cancel(view: View?) {
            dismiss()
        }

        fun ok(view: View?) {
            callBack?.dialogCallBack(
                binding.pickerCampus.contentByCurrValue,
                binding.pickerTime.contentByCurrValue
            )
            dismiss()
        }
    }
}
package com.ahu.ahutong.ui.dialog

import android.app.Dialog
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
                ClassRoomViewModel.campuses.toTypedArray(),
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

    override fun onDestroy() {//当点击外部导致关闭时不会走dismiss，故使用onDestroy
        super.onDestroy()
        callBack = null
        binding.unbind()
    }
}
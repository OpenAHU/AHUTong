package com.ahu.ahutong.ui.dialog

import android.app.Dialog

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.DialogRoomBinding
import com.ahu.ahutong.ui.page.ClassRoomFragment
import com.ahu.ahutong.ui.page.state.ClassRoomViewModel
import com.simon.library.view.BottomDialog
import java.lang.ref.WeakReference

/**
 *
 * @Author: Simon
 * @Date: 2021/8/7-上午12:39
 * @Email: 330771794@qq.com
 */
class ClassRoomDialog(private val campusIndex: Int, private val timesIndex: Int) : BottomDialog() {
    private lateinit var binding: DialogRoomBinding
    private lateinit var callBack: CallBack
    override fun onInitDialog(dialog: Dialog) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_room, null, false)
        dialog.setContentView(binding.root)
        binding.run {
            numberPickerView1.setDividerMarginL(25)
            numberPickerView2.setDividerMarginR(25)
            numberPickerView1.setDividerHeight(1f)
            numberPickerView2.setDividerHeight(1f)
            numberPickerView1.setDividerColor(Color.GRAY)
            numberPickerView2.setDividerColor(Color.GRAY)
            numberPickerView1.setDividerPadding(50f)
            numberPickerView2.setDividerPadding(50f)
            numberPickerView1.setDisplayedValuesAndPickedIndex(ClassRoomViewModel.campus.toTypedArray(),campusIndex,false)
            numberPickerView2.setDisplayedValuesAndPickedIndex(ClassRoomViewModel.times.toTypedArray(),timesIndex,false)
            proxy=ClickProxy()
        }

    }
    fun setCallBack(callBack: CallBack){
        this.callBack=callBack
    }
    interface CallBack{
        fun dialogCallBack(campus:String,time:String)
    }

    override fun dismiss() {
        //这里需要把callback制空（不然会内存泄漏），本人刚学kt，不会制空，等以后再修改
        super.dismiss()
        System.gc()
    }

    inner class ClickProxy {
        fun cancel(view: View?) {
            dismiss()
        }

        fun ok(view: View?) {
            callBack.dialogCallBack(binding.numberPickerView1.contentByCurrValue,binding.numberPickerView2.contentByCurrValue)
            dismiss()
        }
    }
}
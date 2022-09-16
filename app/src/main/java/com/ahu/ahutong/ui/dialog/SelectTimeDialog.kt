package com.ahu.ahutong.ui.dialog

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.databinding.DialogTimeBinding
import com.ahu.ahutong.ext.defaultStyle
import com.ahu.ahutong.ext.getSchoolYears
import com.simon.library.view.BottomDialog

/**
 * @Author: SinkDev
 * @Date: 2021/8/8-下午12:24
 * @Email: 468766131@qq.com
 */
class SelectTimeDialog : BottomDialog(), View.OnClickListener {
    private var callback: CallBack? = null
    private lateinit var binding: DialogTimeBinding
    var isShowing = false

    companion object {
        private val schoolYears by lazy {
            val defaultUser = User()
            defaultUser.name = "Y01717"
            // 求出用户的入学时间
            (
                AHUCache.getCurrentUser()
                    ?: defaultUser
                ).getSchoolYears()
        }
        private val week by lazy {
            val result = mutableListOf<String>()
            for (i in 1..20) {
                result.add("第${i}周")
            }
            result.toTypedArray()
        }
    }

    override fun onInitDialog(dialog: Dialog?) {
        dialog?.setCanceledOnTouchOutside(false)
        cancelBackKey(dialog)
        binding = DialogTimeBinding.inflate(LayoutInflater.from(context))
        dialog?.setContentView(binding.root)
        binding.btOk.setOnClickListener(this)
        binding.btClose.setOnClickListener(this)
        binding.apply {
            pickerSchoolYear.setDividerMarginL(25)
            pickerWeek.setDividerMarginR(25)
            pickerSchoolYear.defaultStyle()
            pickerSchoolTerm.defaultStyle()
            pickerWeek.defaultStyle()
            pickerSchoolYear.setDisplayedValuesAndPickedIndex(
                schoolYears,
                0,
                false
            )
            pickerSchoolTerm.setDisplayedValuesAndPickedIndex(
                arrayOf("第1学期", "第2学期"),
                0,
                false
            )
            pickerWeek.setDisplayedValuesAndPickedIndex(
                week,
                0,
                false
            )
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
        isShowing = true
    }

    override fun dismiss() {
        super.dismiss()
        isShowing = false
    }

    fun setCallBack(callback: CallBack) {
        this.callback = callback
    }

    interface CallBack {
        fun onSelectTime(schoolYear: String, schoolTerm: String, week: Int)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bt_ok -> {
                callback?.onSelectTime(
                    binding.pickerSchoolYear.contentByCurrValue,
                    (binding.pickerSchoolTerm.value + 1).toString(),
                    binding.pickerWeek.value + 1
                )
                dismiss()
            }
            R.id.bt_close -> {
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
        binding.unbind()
    }
}

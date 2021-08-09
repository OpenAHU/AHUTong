package com.ahu.ahutong.ui.dialog

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.DialogTimeBinding
import com.ahu.ahutong.ext.defaultStyle
import com.simon.library.view.BottomDialog
import java.util.*

/**
 * @Author: SinkDev
 * @Date: 2021/8/8-下午12:24
 * @Email: 468766131@qq.com
 */
class SettingTimeDialog: BottomDialog(), View.OnClickListener {
    private var callback: CallBack? = null
    private lateinit var binding: DialogTimeBinding
    companion object{
        private val schoolYears by lazy {
            val result = mutableListOf<String>()
            val instance = Calendar.getInstance(Locale.CHINA)
            val year = instance[Calendar.YEAR]
            for (i in 0..5){
                result.add("${year - i}-${year - i + 1}")
            }
            result.toTypedArray()
        }
        private val week by lazy {
            val result = mutableListOf<String>()
            for (i in 1..20){
                result.add("第${i}周")
            }
            result.toTypedArray()
        }
    }
    override fun onInitDialog(dialog: Dialog?) {
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
                schoolYears, 0, false
            )
            pickerSchoolTerm.setDisplayedValuesAndPickedIndex(
                arrayOf("第1学期" ,"第2学期"), 0, false
            )
            pickerWeek.setDisplayedValuesAndPickedIndex(
                week, 0, false
            )
        }
    }

    fun setCallBack(callback: CallBack){
        this.callback = callback
    }

    interface CallBack{
        fun onSelectTime(schoolYear: String, schoolTerm: String, week: Int)
    }

    override fun dismiss() {
        super.dismiss()
        callback = null
        binding.unbind()
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.bt_ok ->{
                callback?.onSelectTime(
                    binding.pickerSchoolYear.contentByCurrValue,
                    (binding.pickerSchoolTerm.value + 1).toString(),
                    binding.pickerWeek.value + 1
                )
                dismiss()
            }
            R.id.bt_close ->{
                dismiss()
            }

        }

    }
}
package com.ahu.ahutong.ui.page


import android.os.Bundle
import android.view.View
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentClassroomBinding
import com.ahu.ahutong.ui.dialog.ClassRoomDialog
import com.ahu.ahutong.ui.page.state.ClassRoomViewModel

class ClassRoomFragment : BaseFragment<FragmentClassroomBinding>(),ClassRoomDialog.CallBack{
    private lateinit var mState: ClassRoomViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ClassRoomViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_classroom, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    override fun dialogCallBack(campus:String,time:String){
        mState.campus.value=campus
        mState.time.value=time
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun selectTime(view: View){
            val dialog:ClassRoomDialog= ClassRoomDialog()
            dialog.show(parentFragmentManager,"")
            dialog.setCallBack(this@ClassRoomFragment)
        }
    }




}
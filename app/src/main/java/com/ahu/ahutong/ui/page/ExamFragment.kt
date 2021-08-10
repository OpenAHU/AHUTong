package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentExamBinding
import com.ahu.ahutong.databinding.FragmentTeldirectoryBinding
import com.ahu.ahutong.ui.page.state.ExamViewModel
import com.ahu.ahutong.ui.page.state.TelDirectoryViewModel

class ExamFragment : BaseFragment<FragmentExamBinding>() {
    private lateinit var mState: ExamViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ExamViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_exam, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }


}
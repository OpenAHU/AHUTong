package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.util.Log
import android.view.View
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentExamBinding
import com.ahu.ahutong.databinding.FragmentGradleBinding
import com.ahu.ahutong.ui.page.state.ExamViewModel
import com.ahu.ahutong.ui.page.state.GradleViewModel

/**
 * 成绩单
 */
class GradleFragment: BaseFragment<FragmentGradleBinding>() {
    private lateinit var mState: GradleViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(GradleViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_gradle, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      Log.e("test",dataBinding.spSchoolYear.javaClass.name);
    //虽然布局里不是appcompat，但实际上继承appcompatactivity的活动会使用代理工厂，让他变成appcompat的spinner
    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }


}
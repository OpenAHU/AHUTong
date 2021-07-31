package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.MainActivity
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentScheduleBinding
import com.ahu.ahutong.ui.page.state.ScheduleViewModel

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:14
 * @Email 468766131@qq.com
 */
class ScheduleFragment private constructor(): BaseFragment<FragmentScheduleBinding>() {
    private lateinit var mState: ScheduleViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ScheduleViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_schedule, BR.state, mState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setSupportActionBar(dataBinding.toolbar)
    }

    companion object{
        val INSTANCE = ScheduleFragment()
    }

}
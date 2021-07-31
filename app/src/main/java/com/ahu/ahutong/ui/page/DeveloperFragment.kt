package com.ahu.ahutong.ui.page

import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentDeveloperBinding
import com.ahu.ahutong.ui.page.state.DeveloperViewModel

/**
 *
 * @Author: SinkDev
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
class DeveloperFragment() : BaseFragment<FragmentDeveloperBinding>(){
    private lateinit var mState: DeveloperViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(DeveloperViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_developer, BR.state, mState)
    }



}
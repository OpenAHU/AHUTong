package com.ahu.ahutong.ui.page

import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentAboutBinding
import com.ahu.ahutong.ui.page.state.AboutViewModel

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午12:54
 * @Email: 468766131@qq.com
 */
class AboutFragment : BaseFragment<FragmentAboutBinding>() {
    private lateinit var mState: AboutViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(AboutViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_about, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }
}
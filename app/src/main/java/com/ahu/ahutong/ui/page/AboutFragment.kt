package com.ahu.ahutong.ui.page

import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.Constants
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentAboutBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.page.state.AboutViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午12:54
 * @Email: 468766131@qq.com
 */
class AboutFragment : BaseFragment<FragmentAboutBinding>() {
    private lateinit var mState: AboutViewModel
    private lateinit var activityState: MainViewModel
    override fun initViewModel() {
        activityState = getActivityScopeViewModel(MainViewModel::class.java)
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

        fun checkUpdate() {
            activityState.getAppLatestVersion()
        }

        fun updateLog() {
            buildDialog("当前版本更新日志", Constants.UPDATE_LOG, "我知道了").show()
        }
    }
}

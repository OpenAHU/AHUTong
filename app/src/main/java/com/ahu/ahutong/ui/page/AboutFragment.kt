package com.ahu.ahutong.ui.page

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.Constants
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentAboutBinding
import com.ahu.ahutong.ui.page.state.AboutViewModel
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

    override fun observeData() {
        super.observeData()
        // 获取本地版本信息
        val localVersion = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName
        // 主动检查，要提示
        mState.latestVersions.observe(this) { result ->
            result.onSuccess {
                if (!it.isSuccessful) {
                    Toast.makeText(requireContext(), "检查更新失败：${it.msg}", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
                if (it.data.version != localVersion) {
                    MaterialAlertDialogBuilder(requireActivity()).apply {
                        setTitle("更新")
                        setMessage("发现新版本！\n新版特性：\n ${it.data.message}")
                        setPositiveButton("前往下载") { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(it.data.url)
                            startActivity(intent)
                        }
                        setNegativeButton("取消", null)
                    }.show()
                    Toast.makeText(requireContext(), "当前已是最新版本！", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
            }.onFailure {
                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun checkUpdate() {
            mState.getAppLatestVersion()
        }

        fun updateLog() {
            MaterialAlertDialogBuilder(requireActivity()).apply {
                setTitle("当前版本更新日志")
                setMessage(Constants.UPDATE_LOG)
                setPositiveButton("我知道了", null)
            }.show()
        }
    }
}

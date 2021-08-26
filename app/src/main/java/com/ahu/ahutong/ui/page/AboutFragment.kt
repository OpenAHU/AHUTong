package com.ahu.ahutong.ui.page

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.os.persistableBundleOf
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.Constants
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentAboutBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.page.state.AboutViewModel
import com.simon.library.view.LoadingDialog
import com.sink.library.update.CookApkUpdate
import com.sink.library.update.bean.App

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

        fun checkUpdate() {
            val progressDialog = LoadingDialog(context).setMessage("正在加载中...")
            progressDialog.create()
            CookApkUpdate.checkUpdate(object : CookApkUpdate.UpdateListener {
                override fun onNeedUpdate(app: App) {
                    progressDialog.dismiss()
                    val message = "版本：${app.versionName} \n" +
                            "新版特性：\n ${app.intro}"
                    buildDialog("更新", message, "前往下载", { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(app.cookApkUrl)
                        startActivity(intent)
                    }, "取消").show()

                }

                override fun onLatestVersion() {
                    Toast.makeText(requireContext(), "已是最新版本啦", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }

                override fun checkFailure(throwable: Throwable) {
                    Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }

            })
        }

        fun updateLog() {
            buildDialog("更新日志", Constants.UPDATE_LOG, "我知道了").show()
        }
    }
}

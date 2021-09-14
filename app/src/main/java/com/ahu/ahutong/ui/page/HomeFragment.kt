package com.ahu.ahutong.ui.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentHomeBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.page.state.HomeViewModel
import com.simon.library.AppUpdate
import com.sink.library.log.SinkLog
import java.lang.Exception

/**
 * @Author SinkDev
 * @Date 2021/7/27-18:59
 * @Email 468766131@qq.com
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var mState: HomeViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(HomeViewModel::class.java)

    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_home, BR.state, mState)
            .addBindingParam(BR.proxy, ActionProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //pager2
        dataBinding.homeViewPager2.apply {
            isUserInputEnabled = false //禁止滑动
            adapter = object : FragmentStateAdapter(requireActivity()) {

                override fun getItemCount(): Int {
                    return 3
                }

                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> ScheduleFragment.INSTANCE
                        1 -> DiscoveryFragment.INSTANCE
                        2 -> MineFragment.INSTANCE
                        else -> throw RuntimeException("不存在该position")
                    }
                }

            }
        }
        //检查更新
        AppUpdate.check(
            AHUApplication.version,
            object : AppUpdate.CallBack {


                override fun appUpdate(url: String?, msg: String?) {
                    val message = "发现新版本！\n" +
                            "新版特性：\n $msg"
                    Looper.prepare()
                    buildDialog("更新", message, "前往下载", { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }, "取消").show()
                    Looper.loop()
                }

                override fun requestError(e: Exception?) {
                    Looper.prepare()
                    Toast.makeText(requireContext(), "检查更新出错" + e!!.message, Toast.LENGTH_LONG).show()
                    Looper.loop()
                }

                override fun onLatestVersion() {
                    Looper.prepare()
                    Toast.makeText(requireContext(), "已是最新版本啦", Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }

            })
//        CookApkUpdate.checkUpdate(object : CookApkUpdate.UpdateListener {
//            override fun onNeedUpdate(app: App) {
//                val message = "版本：${app.versionName} \n" +
//                        "新版特性：\n ${app.intro}"
//                buildDialog("更新", message, "前往下载", { _, _ ->
//                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.data = Uri.parse(app.cookApkUrl)
//                    startActivity(intent)
//                }, "取消").show()
//            }
//
//            override fun onLatestVersion() {
//                checkVersion()
//            }
//
//            override fun checkFailure(throwable: Throwable) {
//                checkVersion()
//            }
//
//        })

    }

//    private fun checkVersion() {
//        if (AHUCache.getVersionHistory() != mState.versionName) {
//            buildDialog("更新日志", Constants.UPDATE_LOG, "我知道了").show()
//            AHUCache.saveVersionHistory(mState.versionName)
//        }
//    }

    inner class ActionProxy {
        var selectAction: (MenuItem) -> Boolean = fun(item: MenuItem): Boolean {
            val pos = when (item.itemId) {
                R.id.schedule_fragment -> 0
                R.id.discovery_fragment -> 1
                R.id.mine_fragment -> 2
                else -> -1
            }
            if (pos == -1) {
                SinkLog.e("选中的ItemId不存在！")
                return false
            }
            if (dataBinding.homeViewPager2.currentItem == pos) {
                SinkLog.i("已在当前界面！")
                return false
            }
            dataBinding.homeViewPager2.setCurrentItem(pos, false)
            return true
        }
    }
}
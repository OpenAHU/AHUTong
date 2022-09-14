package com.ahu.ahutong.ui.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentHomeBinding
import com.ahu.ahutong.ui.page.state.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @Author SinkDev
 * @Date 2021/7/27-18:59
 * @Email 468766131@qq.com
 */
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var mState: HomeViewModel
    private val scheduleFragment = ScheduleFragment()
    private val discoveryFragment = DiscoveryFragment()
    private val mineFragment = MineFragment()
    private var currentFragment: Fragment = scheduleFragment

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
                    currentFragment = when (position) {
                        0 -> scheduleFragment
                        1 -> discoveryFragment
                        2 -> mineFragment
                        else -> throw RuntimeException("不存在该position")
                    }
                    return currentFragment
                }
            }
        }
        // 检查更新
        mState.getAppLatestVersion()
    }

    override fun observeData() {
        super.observeData()
        // 获取本地版本信息
        val localVersion = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName
        // 默认检查，不提示
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
                    return@onSuccess
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentFragment.onResume()
    }

    inner class ActionProxy {
        var selectAction: (MenuItem) -> Boolean = fun(item: MenuItem): Boolean {
            val pos = when (item.itemId) {
                R.id.schedule_fragment -> 0
                R.id.discovery_fragment -> 1
                R.id.mine_fragment -> 2
                else -> -1
            }
            if (pos == -1) {
                return false
            }
            if (dataBinding.homeViewPager2.currentItem == pos) {
                return false
            }
            dataBinding.homeViewPager2.setCurrentItem(pos, false)
            return true
        }
    }
}
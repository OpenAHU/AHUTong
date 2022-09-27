package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.databinding.FragmentScheduleBinding
import com.ahu.ahutong.ui.dialog.SelectTimeDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:14
 * @Email 468766131@qq.com
 */
class ScheduleFragment : BaseFragment<FragmentScheduleBinding>(), SelectTimeDialog.CallBack {
    private lateinit var mState: ScheduleViewModel
    private lateinit var gState: MainViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ScheduleViewModel::class.java)
        gState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun observeData() {
        super.observeData()
        // 周数
        mState.scheduleConfig.observe(this) {
            dataBinding.viewPager2.setCurrentItem(it.week - 1, false)
        }
        // 因为会存在多个 WeekScheduleFragment 对象，失败会弹出三个toast，所以在这里处理异常
        mState.schedule.observe(this) {
            it.onFailure {
                Toast.makeText(requireContext(), "获取课表失败，${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
        val settingTimeDialog = SelectTimeDialog()
        settingTimeDialog.setCallBack(this)
        // 显示选当前时间的Dialog
        mState.showSelectTimeDialog.observe(this) {
            if (!settingTimeDialog.isShowing) {
                settingTimeDialog.show(parentFragmentManager, "SettingTimeDialog")
            }
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_schedule, BR.state, mState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.viewPager2.apply {
            offscreenPageLimit = 1
            adapter = object : FragmentStateAdapter(this@ScheduleFragment) {
                override fun getItemCount(): Int {
                    return 18
                }

                override fun createFragment(position: Int): Fragment {
                    return WeekScheduleFragment(position + 1)
                }
            }
            isSaveEnabled = false
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                var position = 0
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    this.position = position
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        mState.changeWeek(position + 1)
                    }
                }
            })
        }
        mState.refreshSchedule()
    }

    override fun onSelectTime(schoolYear: String, schoolTerm: String, week: Int) {
        mState.saveTime(schoolYear, schoolTerm, week)
        //刷新
        if (AHUCache.isLogin()) {
            mState.refreshSchedule(schoolYear, schoolTerm)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mState.isInitialized && mState.scheduleConfig.value == null) {
            mState.loadConfig()
        }

    }

}
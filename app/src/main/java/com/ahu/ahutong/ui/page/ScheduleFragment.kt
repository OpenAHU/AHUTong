package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentScheduleBinding
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:14
 * @Email 468766131@qq.com
 */
class ScheduleFragment : BaseFragment<FragmentScheduleBinding>() {
    private lateinit var mState: ScheduleViewModel
    private lateinit var gState: MainViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ScheduleViewModel::class.java)
        gState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun observeData() {
        super.observeData()
        mState.week.observe(this) {
            dataBinding.viewPager2.setCurrentItem(it - 1, false)
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_schedule, BR.state, mState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.viewPager2.apply {
            adapter = object : FragmentStateAdapter(this@ScheduleFragment) {
                override fun getItemCount(): Int {
                    return 18
                }

                override fun createFragment(position: Int): Fragment {
                    return WeekScheduleFragment(position + 1)
                }
            }
            isSaveEnabled = false
            offscreenPageLimit = 1
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                var position = 0
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    this.position = position
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        mState.week.value = position + 1
                    }
                }
            })
        }

    }

    companion object {
        val INSTANCE = ScheduleFragment()
    }


}
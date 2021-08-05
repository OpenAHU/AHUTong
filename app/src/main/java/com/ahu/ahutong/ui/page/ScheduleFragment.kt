package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentScheduleBinding
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:14
 * @Email 468766131@qq.com
 */
class ScheduleFragment: BaseFragment<FragmentScheduleBinding>() {
    private lateinit var mState: ScheduleViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ScheduleViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_schedule, BR.state, mState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse("2021-08-02") ?: Date()
        dataBinding.scheduleView
            .startTime(date)
            .data(emptyList())
            .theme(DefaultDataUtils.getDefaultTheme())
            .loadSchedule()

        dataBinding.scheduleView.setEmptyCourseListener { _, location ->
            Toast.makeText(requireContext(), "${location.startTime} - ${location.weekDay}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        val INSTANCE = ScheduleFragment()
    }

}
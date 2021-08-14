package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentScheduleBinding
import com.ahu.ahutong.ui.dialog.SettingTimeDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.sink.library.log.SinkLog
import java.util.*

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:14
 * @Email 468766131@qq.com
 */
class ScheduleFragment : BaseFragment<FragmentScheduleBinding>(), SettingTimeDialog.CallBack {
    private lateinit var mState: ScheduleViewModel
    private lateinit var activityState: MainViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ScheduleViewModel::class.java)
        activityState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_schedule, BR.state, mState)
    }

    override fun observeData() {
        activityState.isLogin.observe(this){
            if (it){
                mState.refreshSchedule()
                dataBinding.refreshLayout.isRefreshing = true
            }
        }
        //课表数据
        mState.schedule.observe(this) {
            it.onSuccess {
                dataBinding.scheduleView
                    .showAllCourse(true)
                    .data(it)
                    .loadSchedule()
            }
            it.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
            dataBinding.refreshLayout.isRefreshing = false
        }
        //第几周
        mState.week.observe(this){
            dataBinding.scheduleView
                .date(it, Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK])
                .loadSchedule()
        }
        //开学时间
        mState.startTime.observe(this){
            dataBinding.scheduleView
                .startTime(it)
                .loadSchedule()
        }
        //是否显示非本周
        mState.isShowAllCourse.observe(this){
            dataBinding.scheduleView
                .showAllCourse(it)
                .loadSchedule()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mState.schoolTerm.isEmpty() || mState.schoolYear.isEmpty()) {
            //如果没有设定学年学期，开启设置
            val settingTimeDialog = SettingTimeDialog()
            settingTimeDialog.setCallBack(this)
            settingTimeDialog.show(parentFragmentManager, "SettingTimeDialog")
        }
        //加载数据
        dataBinding.scheduleView
            .showAllCourse(mState.isShowAllCourse.value ?: false)
            .startTime(mState.startTime.value ?: Date())
            .date(mState.week.value ?: 1, Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK])
            .loadSchedule()

        //设置点击空课的事件
        dataBinding.scheduleView.setEmptyCourseListener { _, location ->
            Toast.makeText(
                requireContext(),
                "${location.startTime} - ${location.weekDay}",
                Toast.LENGTH_SHORT
            ).show()
        }
        //设置点击课程的事件
        dataBinding.scheduleView.setCourseListener { _, scheduleCourse ->
            Toast.makeText(requireContext(), scheduleCourse.courses[0].name, Toast.LENGTH_SHORT).show()
        }


    }

    override fun onSelectTime(schoolYear: String, schoolTerm: String, week: Int) {
        mState.saveTime(schoolYear, schoolTerm, week)
        //刷新
        if (activityState.isLogin.value == true){
            mState.refreshSchedule(schoolYear, schoolTerm)
            dataBinding.refreshLayout.isRefreshing = true
        }
    }

    companion object {
        val INSTANCE = ScheduleFragment()
    }


}
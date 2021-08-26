package com.ahu.ahutong.ui.page

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentScheduleBinding
import com.ahu.ahutong.databinding.ItemPopCourseBinding
import com.ahu.ahutong.ui.dialog.SettingScheduleDialog
import com.ahu.ahutong.ui.dialog.SettingTimeDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleCourse
import com.ahu.ahutong.widget.ClassWidget
import java.util.*

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:14
 * @Email 468766131@qq.com
 */
class ScheduleFragment : BaseFragment<FragmentScheduleBinding>(), SettingTimeDialog.CallBack,
    SettingScheduleDialog.SettingCallback {
    private lateinit var popupWindow: PopupWindow
    private lateinit var mState: ScheduleViewModel
    private lateinit var gState: MainViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ScheduleViewModel::class.java)
        gState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_schedule, BR.state, mState)
    }

    override fun observeData() {
        gState.isLogin.observe(this) {
            if (it && !(mState.schoolTerm.isEmpty() || mState.schoolYear.isEmpty())) {
                mState.refreshSchedule()
                dataBinding.refreshLayout.isRefreshing = true
            }
        }
        //课表数据
        mState.schedule.observe(this) {
            it.onSuccess {
                dataBinding.scheduleView
                    .data(it)
                    .loadSchedule()
                //更新小部件
                val manager = AppWidgetManager.getInstance(requireContext())
                val componentName = ComponentName(requireActivity(), ClassWidget::class.java)
                val appWidgetIds = manager.getAppWidgetIds(componentName)
                manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
            }
            it.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
            dataBinding.refreshLayout.isRefreshing = false
        }
        //第几周
        mState.week.observe(this) {
            dataBinding.scheduleView
                .date(it, Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK])
                .loadSchedule()
        }
        //开学时间
        mState.startTime.observe(this) {
            dataBinding.scheduleView
                .startTime(it)
                .loadSchedule()
        }
        //是否显示非本周
        mState.isShowAllCourse.observe(this) {
            dataBinding.scheduleView
                .showAllCourse(it)
                .loadSchedule()
        }
        //主题
        gState.scheduleTheme.observe(this) {
            dataBinding.scheduleView.theme(it)
                .loadSchedule()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mState.schoolTerm.isEmpty() || mState.schoolYear.isEmpty()) {
            //如果没有设定学年学期，开启设置
            val settingTimeDialog = SettingTimeDialog()
            settingTimeDialog.setCallBack(this)
            settingTimeDialog.show(parentFragmentManager, "SettingTimeDialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //加载数据
        dataBinding.scheduleView
            .showAllCourse(mState.isShowAllCourse.value ?: false)
            .startTime(mState.startTime.value ?: Date())
            .date(mState.week.value ?: 1, Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK])
            .loadSchedule()


        //设置点击空课的事件
        dataBinding.scheduleView.setEmptyCourseListener { _, location ->

        }

        //设置点击课程的事件
        dataBinding.scheduleView.setCourseListener { v, scheduleCourse ->
            popupWindow = PopupWindow(
                v, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true
            )
            popupWindow.animationStyle = R.style.pop_anim_style
            popupWindow.contentView = getPopupView(scheduleCourse)
            popupWindow.isTouchable = true
            popupWindow.isOutsideTouchable = true
            popupWindow.showAsDropDown(v)

        }

        //兔子
        dataBinding.scheduleView.setSettingClickListener {
            val dialog = SettingScheduleDialog()
            dialog.setCallback(this)
            dialog.show(parentFragmentManager, "SettingScheduleDialog")
        }

        dataBinding.refreshLayout.setOnRefreshListener {
            mState.refreshSchedule()
        }


    }

    /**
     * 创建课程详情界面
     * @param scheduleCourse ScheduleCourse
     * @return NestedScrollView
     */
    private fun getPopupView(scheduleCourse: ScheduleCourse): NestedScrollView {
        //创建课程视图的父容器
        val li = LinearLayout(context)
        li.orientation = LinearLayout.VERTICAL
        li.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        //填入课程视图
        for (course in scheduleCourse.courses) {
            val binding = ItemPopCourseBinding.inflate(layoutInflater)
            binding.course = course
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.bottomMargin = 10
            binding.root.layoutParams = layoutParams
            li.addView(binding.root)
            binding.tvMore.setOnClickListener {
                //点击消失
                popupWindow.dismiss()
                val bundle = Bundle().apply {
                    putBoolean("add", false)
                    putSerializable("course", course)
                }
                nav().navigate(R.id.course_fragment, bundle)
            }
        }
        val nestedScrollView = NestedScrollView(requireContext())
        nestedScrollView.addView(li)
        return nestedScrollView
    }

    override fun onSelectTime(schoolYear: String, schoolTerm: String, week: Int) {
        mState.saveTime(schoolYear, schoolTerm, week)
        //刷新
        if (gState.isLogin.value == true) {
            mState.refreshSchedule(schoolYear, schoolTerm)
            dataBinding.refreshLayout.isRefreshing = true
        }
    }

    companion object {
        val INSTANCE = ScheduleFragment()
    }

    // 以下是兔子Dialog的实现
    override fun addCourse() {
        val bundle = Bundle().apply {
            putBoolean("add", true)
        }
        nav().navigate(R.id.course_fragment, bundle)
    }

    override fun setStartTime() {
        val settingTimeDialog = SettingTimeDialog()
        settingTimeDialog.setCallBack(this)
        settingTimeDialog.show(parentFragmentManager, "SettingTimeDialog")
    }

    override fun inputSchedule() {
        mState.refreshSchedule(isRefresh = true)
        dataBinding.refreshLayout.isRefreshing = true
    }

    override fun gotoSetting() {
        nav().navigate(R.id.setting_fragment)
    }
    //到这里


}
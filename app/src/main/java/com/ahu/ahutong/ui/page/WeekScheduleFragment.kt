package com.ahu.ahutong.ui.page

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.R
import com.ahu.ahutong.BR
import com.ahu.ahutong.databinding.FragmentScheduleWeekBinding
import com.ahu.ahutong.databinding.ItemPopCourseBinding
import com.ahu.ahutong.ext.buildDialog
import com.ahu.ahutong.ui.dialog.ChooseOneDialog
import com.ahu.ahutong.ui.dialog.SettingScheduleDialog
import com.ahu.ahutong.ui.dialog.SettingTimeDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleCourse
import com.ahu.ahutong.widget.ClassWidget
import java.util.*

/**
 * @Author: SinkDev
 * @Date: 2021/8/27-下午3:33
 * @Email: 468766131@qq.com
 */
class WeekScheduleFragment(val week: Int) : BaseFragment<FragmentScheduleWeekBinding>(),
    SettingTimeDialog.CallBack,
    SettingScheduleDialog.SettingCallback {
    constructor() : this(1)

    private lateinit var popupWindow: PopupWindow
    private lateinit var pState: ScheduleViewModel
    private lateinit var gState: MainViewModel

    override fun initViewModel() {
        parentFragment?.let {
            pState = ViewModelProvider(it).get(ScheduleViewModel::class.java)
        }

        gState = getActivityScopeViewModel(MainViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_schedule_week, BR.state, pState)
    }


    override fun observeData() {
        gState.isLogin.observe(this) {
            if (it && !(pState.schoolTerm.isEmpty() || pState.schoolYear.isEmpty())) {
                pState.refreshSchedule()
                dataBinding.refreshLayout.isRefreshing = true
            }
        }
        //课表数据
        pState.schedule.observe(this) {
            it.onSuccess {
                dataBinding.scheduleView
                    .data(it)
                if (!isResumed) {
                    return@onSuccess
                }
                if (it.isEmpty()) {
                    buildDialog(
                        "提示",
                        "当前学期课表数据为空，请查看是否选择了正确的学年学期", "确定"
                    ).show()
                }
                dataBinding.scheduleView.loadSchedule()
                //更新小部件
                val manager = AppWidgetManager.getInstance(requireContext())
                val componentName = ComponentName(requireActivity(), ClassWidget::class.java)
                val appWidgetIds = manager.getAppWidgetIds(componentName)
                manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)

            }
            it.onFailure {
                if (!isResumed) {
                    return@onFailure
                }
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
            //停止加载
            dataBinding.refreshLayout.isRefreshing = false
        }

        //开学时间
        pState.startTime.observe(this) {
            dataBinding.scheduleView
                .startTime(it)
            if (isResumed) {
                dataBinding.scheduleView.loadSchedule()
            }
        }

        //是否显示非本周
        pState.isShowAllCourse.observe(this) {
            dataBinding.scheduleView
                .showAllCourse(it)
            if (isResumed) {
                dataBinding.scheduleView.loadSchedule()
            }
        }

        //主题
        gState.scheduleTheme.observe(this) {
            dataBinding.scheduleView.theme(it)
                .loadSchedule()
        }
    }


    override fun onResume() {
        super.onResume()
        if (pState.schoolTerm.isEmpty() || pState.schoolYear.isEmpty()) {
            //如果没有设定学年学期，开启设置
            val settingTimeDialog = SettingTimeDialog()
            settingTimeDialog.setCallBack(this)
            settingTimeDialog.show(parentFragmentManager, "SettingTimeDialog")
        }
        //懒加载
        dataBinding.scheduleView.loadSchedule()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //加载数据
        dataBinding.scheduleView
            .showAllCourse(pState.isShowAllCourse.value ?: false)
            .startTime(pState.startTime.value ?: Date())
            .date(week, Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK])

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

        //切换周数
        dataBinding.scheduleView.setChangeWeekListener {
            val list = mutableListOf<String>()
            for (i in 1..18) {
                list.add("第 $i 周")
            }
            val chooseOneDialog = ChooseOneDialog(list)
            chooseOneDialog.selectListener = { index, _ ->
                pState.week.value = index + 1
            }
            chooseOneDialog.show(parentFragmentManager, "chooseStartWeek")
        }

        //刷新
        dataBinding.refreshLayout.setOnRefreshListener {
            pState.refreshSchedule()
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
        pState.saveTime(schoolYear, schoolTerm, week)
        //刷新
        if (gState.isLogin.value == true) {
            pState.refreshSchedule(schoolYear, schoolTerm)
            dataBinding.refreshLayout.isRefreshing = true
        }
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
        pState.refreshSchedule(isRefresh = true)
        dataBinding.refreshLayout.isRefreshing = true
    }

    override fun gotoSetting() {
        nav().navigate(R.id.setting_fragment)
    }
}
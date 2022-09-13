package com.ahu.ahutong.ui.page

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.R
import com.ahu.ahutong.BR
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.databinding.FragmentScheduleWeekBinding
import com.ahu.ahutong.databinding.ItemPopCourseBinding
import com.ahu.ahutong.ui.dialog.ChooseOneDialog
import com.ahu.ahutong.ui.dialog.SelectScheduleDialog
import com.ahu.ahutong.ui.page.state.MainViewModel
import com.ahu.ahutong.ui.page.state.ScheduleViewModel
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleCourse
import com.ahu.ahutong.widget.ClassWidget
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @Author: SinkDev
 * @Date: 2021/8/27-下午3:33
 * @Email: 468766131@qq.com
 */
class WeekScheduleFragment(val week: Int) : BaseFragment<FragmentScheduleWeekBinding>(),
    SelectScheduleDialog.SettingCallback {
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
        // 不可以在数据观察后
        pState.scheduleConfig.observe(this) {
            dataBinding.scheduleView
                .date(it.week, it.weekDay)
                .theme(it.theme)
                .startTime(it.startTime)
                .showAllCourse(it.isShowAll)
                .loadSchedule()
        }

        //课表数据
        pState.schedule.observe(this) { result ->
            result.onSuccess {
                if (it.isEmpty()) {
                    MaterialAlertDialogBuilder(requireActivity()).apply {
                        setTitle("提示")
                        setMessage("当前学期课表数据为空，请查看是否选择了正确的学年学期")
                        setPositiveButton("确定", null)
                    }.show()
                }
                // 课表数据刷新
                dataBinding.scheduleView
                    .data(it)
                    .loadSchedule()
                //更新小部件
                val manager = AppWidgetManager.getInstance(requireContext())
                val componentName = ComponentName(requireActivity(), ClassWidget::class.java)
                val appWidgetIds = manager.getAppWidgetIds(componentName)
                manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview)
            }
            //停止加载
            dataBinding.refreshLayout.isRefreshing = false
        }


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //设置点击课程的事件
        dataBinding.scheduleView.setCourseListener { v, scheduleCourse ->
            val popupView = getPopupView(scheduleCourse)
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            popupWindow = PopupWindow(v, popupView.measuredWidth, popupView.measuredHeight, true)
            popupWindow.animationStyle = R.style.pop_anim_style
            popupWindow.contentView = popupView
            popupWindow.isTouchable = true
            popupWindow.isOutsideTouchable = true
            popupWindow.showAsDropDown(v)

        }

        //兔子
        dataBinding.scheduleView.setSettingClickListener {
            val dialog = SelectScheduleDialog()
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
                pState.changeWeek(index + 1)
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


    // 以下是兔子Dialog的实现
    override fun addCourse() {
        val bundle = Bundle().apply {
            putBoolean("add", true)
        }
        nav().navigate(R.id.course_fragment, bundle)
    }

    override fun setStartTime() {
        pState.showSelectTimeDialog.call()
    }

    override fun inputSchedule() {
        if (!AHUCache.isLogin()) {
            Toast.makeText(requireContext(), "登录后才可以刷新课表哦！", Toast.LENGTH_SHORT).show()
            return
        }
        pState.refreshSchedule(isRefresh = true)
        dataBinding.refreshLayout.isRefreshing = true
    }

    override fun gotoSetting() {
        nav().navigate(R.id.setting_fragment)
    }
}
package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.databinding.FragmentCourseBinding
import com.ahu.ahutong.ui.dialog.ChooseOneDialog
import com.ahu.ahutong.ui.page.state.CourseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @Author: SinkDev
 * @Date: 2021/8/26-下午3:01
 * @Email: 468766131@qq.com
 */
class CourseFragment : BaseFragment<FragmentCourseBinding>() {
    private var isAdd = false
    private lateinit var mState: CourseViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(CourseViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        isAdd = arguments?.getBoolean("add") ?: false
        mState.course.value = if (isAdd) {
            Course()
        } else {
            arguments?.getSerializable("course") as Course
        }
        return DataBindingConfig(R.layout.fragment_course, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
            .addBindingParam(BR.add, isAdd)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun saveOrAddCourse() {
            val course = mState.course.value ?: return
            //读取一波输入内容
            updateInputDate(course)
            course.apply {
                if (length == 0 || name.isNullOrEmpty()
                    || teacher.isNullOrEmpty() || location.isNullOrEmpty()
                ) {
                    Toast.makeText(requireContext(), "请输入完整课程信息", Toast.LENGTH_SHORT).show()
                }
                if (length <= 0 || length >= 11) {
                    Toast.makeText(requireContext(), "请输入正确的课程长度", Toast.LENGTH_SHORT).show()
                    return
                }
                if (course.length + course.startTime > 12) {
                    Toast.makeText(requireContext(), "一天最多有11节课", Toast.LENGTH_SHORT).show()
                    return
                }
                if (startWeek <= 0 || endWeek < startWeek) {
                    Toast.makeText(requireContext(), "请选择正确的起止周数", Toast.LENGTH_SHORT).show()
                    return
                }
                if (weekday == 0) {
                    Toast.makeText(requireContext(), "请选择星期", Toast.LENGTH_SHORT).show()
                    return
                }
                //添加的新信息
                if (isAdd) {
                    extra = ""
                    courseId = System.currentTimeMillis().toString()
                }
            }
            //显示dialog
            MaterialAlertDialogBuilder(requireActivity()).apply {
                setTitle("提示")
                setMessage("是否确认添加该课程?")
                setPositiveButton("确定") { _, _ ->
                    mState.addCourse(course)
                    Toast.makeText(requireContext(), "添加成功, 返回刷新即可", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("取消", null)
            }.show()
        }

        fun deleteCourse() {
            val course = arguments?.getSerializable("course") as Course
            MaterialAlertDialogBuilder(requireActivity()).apply {
                setTitle("提示")
                setMessage("是否确认删除该课程, 本地添加课程移除后不可恢复！")
                setPositiveButton("确定") { _, _ ->
                    mState.removeCourse(course)
                    Toast.makeText(requireContext(), "移除成功, 返回刷新即可", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton("取消", null)
            }.show()
        }

        /**
         * 选择周几
         */
        fun chooseWeekday() {
            val list = mutableListOf<String>()
            for (i in 1..7) {
                list.add("周 $i")
            }
            val chooseOneDialog = ChooseOneDialog(list)

            chooseOneDialog.selectListener = { index, _ ->
                val course = mState.course.value
                course?.setWeekday((index + 1).toString())
                updateInputDate(course)
                mState.course.value = course
            }
            chooseOneDialog.show(parentFragmentManager, "chooseWeekday")
        }

        fun chooseStartWeek() {
            val list = mutableListOf<String>()
            for (i in 1..25) {
                list.add("第 $i 周")
            }
            val chooseOneDialog = ChooseOneDialog(list)
            chooseOneDialog.selectListener = { index, _ ->
                val course = mState.course.value
                course?.setStartWeek((index + 1).toString())
                updateInputDate(course)
                mState.course.value = course
            }
            chooseOneDialog.show(parentFragmentManager, "chooseStartWeek")
        }


        fun chooseEndWeek() {
            val course = mState.course.value
            if (course == null || course.startWeek == 0) {
                Toast.makeText(requireContext(), "请先填写开始周数!", Toast.LENGTH_SHORT).show()
                return
            }
            val list = mutableListOf<String>()
            for (i in course.startWeek..25) {
                list.add("第 $i 周")
            }
            val chooseOneDialog = ChooseOneDialog(list)
            chooseOneDialog.selectListener = { index, _ ->
                course.setEndWeek((index + course.startWeek).toString())
                updateInputDate(course)
                mState.course.value = course
            }
            chooseOneDialog.show(parentFragmentManager, "chooseEndWeek")
        }

        fun chooseStartTime() {
            val list = mutableListOf<String>()
            for (i in 1..11) {
                list.add("第 $i 节")
            }
            val chooseOneDialog = ChooseOneDialog(list)
            chooseOneDialog.selectListener = { index, _ ->
                val course = mState.course.value
                course?.setStartTime((index + 1).toString())
                updateInputDate(course)
                mState.course.value = course
            }
            chooseOneDialog.show(parentFragmentManager, "chooseStartWeek")
        }

        /**
         * 把输入的内容绑定上
         * @param course Course?
         */
        private fun updateInputDate(course: Course?) {
            val length = dataBinding.edCourseLength.text.toString()
            val name = dataBinding.edCourseName.text.toString()
            val teacher = dataBinding.edCourseTeacher.text.toString()
            val location = dataBinding.edCourseLocation.text.toString()
            course?.apply {
                setLength(length)
                setName(name)
                setTeacher(teacher)
                setLocation(location)
            }
        }
    }
}
package com.ahu.ahutong.ui.adapter.holder

import androidx.recyclerview.widget.LinearLayoutManager
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.databinding.ItemCourseBinding
import com.ahu.ahutong.databinding.ItemDiscoveryCourseBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder

class CourseItemHolder(val binding: ItemDiscoveryCourseBinding, courseClickAction: (Course) -> Unit) :
    BaseViewHolder<ItemDiscoveryCourseBinding, List<Course>>(binding) {
    val courseClickProxy = CourseClickProxy(courseClickAction)
    override fun bind(data: List<Course>) {
        binding.rv.layoutManager =
            LinearLayoutManager(binding.root.context)
        binding.rv.adapter = object : BaseAdapter<Course, ItemCourseBinding>(data) {
            override fun layout(): Int {
                return R.layout.item_course
            }

            override fun bindingData(binding: ItemCourseBinding, data: Course) {
                binding.bean = data
                binding.proxy = courseClickProxy
            }
        }
    }

    inner class CourseClickProxy(val action: (Course)-> Unit) {
        fun onClick(course: Course) {
            action(course)
        }
    }
}
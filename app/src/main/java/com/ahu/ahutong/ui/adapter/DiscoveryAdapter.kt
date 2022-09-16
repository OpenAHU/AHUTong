package com.ahu.ahutong.ui.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Tool
import com.ahu.ahutong.databinding.*
import com.ahu.ahutong.ui.adapter.holder.ActivityItemHolder
import com.ahu.ahutong.ui.adapter.holder.BannerItemHolder
import com.ahu.ahutong.ui.adapter.holder.CourseItemHolder
import com.ahu.ahutong.ui.adapter.holder.ToolsItemHolder


class DiscoveryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var activityBean: ActivityBean
    private var banners: List<Banner>
    private val tools: List<Tool>
    private var courses: List<Course>
    var toolItemSelectAction: ((Tool) -> Unit) = {}
    var courseClickAction: (Course) -> Unit = {}

    //Tip: 类似Java, kotlin的构造函数体，尽量写在第一个方法的位置
    init {
        banners = mutableListOf()
        tools = Tool.defaultTools
        courses = mutableListOf()
        activityBean = ActivityBean("0.00","0.00", "桔园:女生\n竹园:均可\n惠园:均")
    }


    //Tip: 为了让每个方法的代码更加清晰，建议使用多个ViewHolder，不同的ItemView使用不同的ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_discovery_banner -> {
                val binding: ItemDiscoveryBannerBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_banner,
                    parent,
                    false
                )
                val bannerItemHolder = BannerItemHolder(binding)
                bannerItemHolder.bind(banners)
                bannerItemHolder
            }
            R.layout.item_discovery_tools -> {
                val binding: ItemDiscoveryToolsBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_tools,
                    parent,
                    false
                )
                val toolsItemHolder = ToolsItemHolder(binding, toolItemSelectAction)
                toolsItemHolder.bind(tools)
                toolsItemHolder
            }
            R.layout.item_discovery_course -> {

                val binding: ItemDiscoveryCourseBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_course,
                    parent,
                    false
                )
                val coursesItemHolder = CourseItemHolder(binding, courseClickAction)
                coursesItemHolder.bind(courses)
                coursesItemHolder
            }
            else -> {
                val binding: ItemDiscoveryActivityBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_activity,
                    parent,
                    false
                )
                val activityItemHolder = ActivityItemHolder(binding)
                activityItemHolder
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is BannerItemHolder->{
                holder.bind(banners)
            }
            is ActivityItemHolder->{
                holder.bind(activityBean)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        //Tip: 常量类似Viewtype这种 不建议使用字面常量 不利于维护，使用有意义的常量名或者R.layout.xxx
        return when (position) {
            0 -> R.layout.item_discovery_banner
            1 -> R.layout.item_discovery_tools
            2 -> R.layout.item_discovery_course
            else -> R.layout.item_discovery_activity
        }
    }

    override fun getItemCount(): Int {
        return 4
    }


    data class ActivityBean(val money: String,val transitionBalance:String, val bathrooms: String)


    fun setBanners(data: List<Banner>) {
        banners = data
        notifyItemChanged(0)
    }

    fun setCourses(data: List<Course>) {
        courses = data
        notifyItemChanged(2)
    }


    fun setActivityBean(activityBean: ActivityBean) {
        this.activityBean = activityBean
        notifyItemChanged(3)
    }
}
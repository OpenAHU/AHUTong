package com.ahu.ahutong.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.databinding.*
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder
import com.ahu.ahutong.ui.page.BathViewContainer
import com.ahu.ahutong.ui.page.DiscoveryFragment
import com.ahu.ahutong.ui.widget.banner.BannerView.BannerAdapter
import com.simon.library.ViewContainer

/*
最后一个item的布局层如下
item_discovery_activity
  item_activity
   itemXXX
*/

class DiscoveryAdapter(bean: DiscoveryBean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var banners: List<Banner>
    private val tools: List<Tool>
    private var courses: List<Course>
    private val activitys:List<ViewContainer>

    //Tip: 类似Java, kotlin的构造函数体，尽量写在第一个方法的位置
    init {
        banners = bean.banners
        tools = bean.tools
        courses = bean.courses
        activitys = listOf(BathViewContainer(0),BathViewContainer(1))//这里设置活动
    }

    fun setBanners(data: List<Banner>) {
        banners = data
    }
    fun setCourses(data: List<Course>) {
        courses = data
    }


    //Tip: 为了让每个方法的代码更加清晰，建议使用多个ViewHolder，不同的ItemView使用不同的ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.e("TAG","??")
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
                val toolsItemHolder = ToolsItemHolder(binding)
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
                val coursesItemHolder = CourseItemHolder(binding)
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
                activityItemHolder.bind(activitys)
                activityItemHolder
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (getItemViewType(position) == R.layout.item_discovery_news) {
//            (holder as NewsItemHolder).bind(news[position - 3])
//        }
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



    data class DiscoveryBean(
        val banners: List<Banner>,
        val tools: List<Tool>,
        val courses: List<Course>
    )

    inner class BannerItemHolder(val binding: ItemDiscoveryBannerBinding) :
        BaseViewHolder<ItemDiscoveryBannerBinding, List<Banner>>(binding) {
        override fun bind(data: List<Banner>) {
            val banners = arrayListOf(R.mipmap.banner0, R.mipmap.banner1, R.mipmap.banner2)
            binding.itemDiscoveryBanner.setAdapter(object : BannerAdapter() {
                override fun getItemCount(): Int {
                    return  if(banners.size == 0)  banners.size else banners.size
                }

                override fun getImageView(parentView: View, position: Int): ImageView {
                    if (data.isEmpty()){
                        val imageView = ImageView(parentView.context)
                        imageView.scaleType = ImageView.ScaleType.FIT_XY
                        imageView.load(banners[position])
                        return imageView
                    }
                    val imageView = ImageView(parentView.context)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                    val banner =  data[position]
                    imageView.load(banner.imageUrl)
                    return imageView
                }
            })
        }
    }

    inner class ToolsItemHolder(val binding: ItemDiscoveryToolsBinding) :
        BaseViewHolder<ItemDiscoveryToolsBinding, List<Tool>>(binding) {
        override fun bind(data: List<Tool>) {
            binding.itemDiscoveryTools.layoutManager = GridLayoutManager(binding.root.context, 5)
            binding.itemDiscoveryTools.adapter = object : BaseAdapter<Tool, ItemToolBinding>(data) {
                override fun layout(): Int {
                    return R.layout.item_tool
                }

                override fun bindingData(binding: ItemToolBinding, data: Tool) {
                    binding.tool = data
                    binding.proxy = DiscoveryFragment.INSTANCE.ToolClickProxy()
                }
            }
        }
    }
    inner class CourseItemHolder(val binding: ItemDiscoveryCourseBinding) :
        BaseViewHolder<ItemDiscoveryCourseBinding, List<Course>>(binding) {
        override fun bind(data: List<Course>) {
            binding.rv.layoutManager = LinearLayoutManager(binding.root.context,RecyclerView.HORIZONTAL,false)
            binding.rv.adapter = object : BaseAdapter<Course, ItemCourseBinding>(data) {
                override fun layout(): Int {
                    return R.layout.item_course
                }

                override fun bindingData(binding: ItemCourseBinding, data: Course) {
                    binding.bean = data
                    binding.proxy = DiscoveryFragment.INSTANCE.CourseClickProxy()

                }
            }
        }
    }
    inner class ActivityItemHolder(val binding: ItemDiscoveryActivityBinding) :
        BaseViewHolder<ItemDiscoveryActivityBinding, List<ViewContainer>>(binding) {
        override fun bind(data: List<ViewContainer>) {
            binding.rv.layoutManager = LinearLayoutManager(binding.root.context,RecyclerView.HORIZONTAL,false)
            binding.rv.adapter = object : BaseAdapter<ViewContainer, ItemActivityBinding>(data) {
                override fun layout(): Int {
                    return R.layout.item_activity
                }

                override fun bindingData(binding: ItemActivityBinding, data: ViewContainer) {
                    data.createView(binding.root,binding.root.context)
                }
            }
        }
    }

}
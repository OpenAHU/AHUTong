package com.ahu.ahutong.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Tool
import com.ahu.ahutong.databinding.*
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder
import com.ahu.ahutong.ui.page.DiscoveryFragment
import com.ahu.ahutong.ui.widget.banner.BannerView.BannerAdapter
import com.sink.library.log.SinkLog


class DiscoveryAdapter(bean: DiscoveryBean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var activityBean: DiscoveryAdapter.ActivityBean
    private var banners: List<Banner>
    private val tools: List<Tool>
    private var courses: List<Course>

    //Tip: 类似Java, kotlin的构造函数体，尽量写在第一个方法的位置
    init {
        banners = bean.banners
        tools = bean.tools
        courses = bean.courses
        activityBean = bean.activityBean
    }

    fun setBanners(data: List<Banner>) {
        banners = data
        notifyItemChanged(0)
    }

    fun setCourses(data: List<Course>) {
        courses = data
    }


    fun setActivityBean(activityBean: ActivityBean) {
        this.activityBean = activityBean
        notifyItemChanged(3)
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
                activityItemHolder
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ActivityItemHolder) {
            holder.bind(activityBean)
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


    data class DiscoveryBean(
        val banners: List<Banner>,
        val tools: List<Tool>,
        val courses: List<Course>,
        val activityBean: ActivityBean
    )

    inner class BannerItemHolder(val binding: ItemDiscoveryBannerBinding) :
        BaseViewHolder<ItemDiscoveryBannerBinding, List<Banner>>(binding) {
        override fun bind(data: List<Banner>) {

            binding.itemDiscoveryBanner.setAdapter(object : BannerAdapter() {
                override fun getItemCount(): Int {
                    return if (data.isEmpty()) 0 else data.size
                }

                override fun getImageView(parentView: View, position: Int): ImageView {
                    val imageView = ImageView(parentView.context)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                    val banner = data[position]
                    imageView.load(banner.imageUrl)
                    imageView.setOnClickListener {
                        try {
                            val i1 = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(banner.detailUrl)
                            )
                            i1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            it.context.startActivity(i1)
                        } catch (e: Exception) {
                            SinkLog.e(e)
                        }
                    }
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
            binding.rv.layoutManager =
                LinearLayoutManager(binding.root.context)
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
        BaseViewHolder<ItemDiscoveryActivityBinding, ActivityBean>(binding) {
        override fun bind(data: ActivityBean) {
            binding.bean = data
            binding.tvPay.setOnClickListener {
                try {
                    val i1 = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("alipays://platformapi/startapp?appId=2019090967125695&page=pages%2Findex%2Findex&enbsv=0.3.2106171038.6&chInfo=ch_share__chsub_CopyLink")
                    )
                    i1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    it.context.startActivity(i1)
                } catch (e: Exception) {
                    Toast.makeText(it.context, "手机未安装支付宝App", Toast.LENGTH_SHORT).show()
                }
            }
            binding.tvFlow.setOnClickListener {
                Toast.makeText(it.context, "开发中，敬请期待.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ActivityBean(val money: String, val north: String, val south: String)

}
package com.ahu.ahutong.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.*
import com.ahu.ahutong.databinding.*
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder
import com.ahu.ahutong.ui.page.DiscoveryFragment
import com.ahu.ahutong.ui.widget.banner.BannerView.BannerAdapter


class DiscoveryAdapter(bean: DiscoveryBean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var banners: List<Banner>
    private val tools: List<Tool>
    private var news: MutableList<News>

    //Tip: 类似Java, kotlin的构造函数体，尽量写在第一个方法的位置
    init {
        banners = bean.banners
        tools = bean.tools
        news = bean.news
    }

    fun setBanners(data: List<Banner>) {
        banners = data
    }

    fun setNews(data: MutableList<News>) {
        news = data
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
            R.layout.item_discovery_sector -> {
                val binding: ItemDiscoverySectorBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_sector,
                    parent,
                    false
                )
                val sectorItemHolder = SectorItemHolder(binding)
                sectorItemHolder.bind(Unit)
                sectorItemHolder
            }
            else -> {
                val binding: ItemDiscoveryNewsBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_news,
                    parent,
                    false
                )
                NewsItemHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == R.layout.item_discovery_news) {
            (holder as NewsItemHolder).bind(news[position - 3])
        }
    }

    override fun getItemViewType(position: Int): Int {
        //Tip: 常量类似Viewtype这种 不建议使用字面常量 不利于维护，使用有意义的常量名或者R.layout.xxx
        return when (position) {
            0 -> R.layout.item_discovery_banner
            1 -> R.layout.item_discovery_tools
            2 -> R.layout.item_discovery_sector
            else -> R.layout.item_discovery_news
        }
    }

    override fun getItemCount(): Int {
        return news.size + 3
    }

    //Tip: ClickProxy 放在fragment， 不然是没有nav用的


    data class DiscoveryBean(
        val banners: List<Banner>,
        val tools: List<Tool>,
        val news: MutableList<News>
    )

    inner class BannerItemHolder(val binding: ItemDiscoveryBannerBinding) :
        BaseViewHolder<ItemDiscoveryBannerBinding, List<Banner>>(binding) {
        override fun bind(data: List<Banner>) {
            binding.itemDiscoveryBanner.setAdapter(object : BannerAdapter() {
                override fun getItemCount(): Int {
                    return banners.size
                }

                override fun getImageView(parentView: View, position: Int): ImageView {
                    val imageView = ImageView(parentView.context)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                    val banner = data[position]
                    imageView.load(banner.imgPath)
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

    inner class SectorItemHolder(val binding: ItemDiscoverySectorBinding) :
        BaseViewHolder<ItemDiscoverySectorBinding, Unit>(binding) {
        override fun bind(data: Unit) {
            binding.btRadios.setOnCheckedChangeListener { _, checkedId ->
                val type = when (checkedId) {
                    R.id.rd_college -> NewsType.College
                    R.id.rd_association -> NewsType.Association
                    R.id.rd_edu -> NewsType.Edu
                    R.id.rd_recruit -> NewsType.Recruit
                    else -> NewsType.College
                }
                //fragment处理事件
                DiscoveryFragment.INSTANCE.SectorClickProxy().onClick(type)
            }
        }

    }

    inner class NewsItemHolder(val binding: ItemDiscoveryNewsBinding) :
        BaseViewHolder<ItemDiscoveryNewsBinding, News>(binding) {
        override fun bind(data: News) {
            binding.news = data
            binding.proxy = DiscoveryFragment.INSTANCE.NewsClickProxy()
        }

    }


}
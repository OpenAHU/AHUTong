package com.ahu.ahutong.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.GridItem
import com.ahu.ahutong.data.model.News
import com.ahu.ahutong.databinding.*
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter.DiscoveryViewHolder
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.widget.banner.BannerView.BannerAdapter


class DiscoveryAdapter : RecyclerView.Adapter<DiscoveryViewHolder>() {
    private var gridItems: List<GridItem>? = null
    private var banners: ArrayList<Banner>? = null
    private val datas = ArrayList<News>()
    fun addGridItem(items: List<GridItem>) {
        gridItems = items
    }

    fun addNews(news: List<News>?) {
        datas.addAll(news!!)
    }

    fun setBanners(banners: ArrayList<Banner>?) {
        this.banners = banners
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoveryViewHolder {
        val holder: DiscoveryViewHolder
        when (viewType) {
            0 -> {
                val binding: ItemDiscoveryBannerBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_banner,
                    parent,
                    false
                )
                holder = DiscoveryViewHolder(binding.root)
                binding.itemDiscoveryBanner.setBackgroundColor(Color.BLUE)
                var bannerAdapter: BannerAdapter?
                binding.itemDiscoveryBanner.setAdapter(object : BannerAdapter() {
                    override fun getItemCount(): Int {
                        return if (banners == null) 0 else banners!!.size
                    }

                    override fun getImageView(parentView: View, position: Int): ImageView {
                        val imageView = ImageView(parentView.context)
                        imageView.scaleType = ImageView.ScaleType.FIT_XY
                        val banner = banners!![position]
                        imageView.load(banner.imgPath)
                        return imageView
                    }
                }.also { bannerAdapter = it })
            }
            1 -> {
                val binding: ItemDiscoveryGridBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_grid,
                    parent,
                    false
                )
                holder = DiscoveryViewHolder(binding.root)
                binding.itemDiscoveryGrid.layoutManager = GridLayoutManager(parent.context, 5)
                binding.itemDiscoveryGrid.adapter = object: BaseAdapter<GridItem, ItemGridBinding>(gridItems!!){
                    override fun layout(): Int {
                        return R.layout.item_grid
                    }
                    override fun bindingData(binding: ItemGridBinding, data: GridItem) {
                        binding.dev = data
                        binding.proxy = ClickProxy()
                    }
                }
            }
            else -> {
                val binding: ItemDiscoveryNewsBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_discovery_news,
                    parent,
                    false
                )
                holder = DiscoveryViewHolder(binding.root)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: DiscoveryViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding<ViewDataBinding>(holder.itemView)
        if (getItemViewType(position) == 2) {
            val newsBinding = binding as ItemDiscoveryNewsBinding?
            newsBinding!!.itemDiscoveryText.text = "ksfkhfskshfkhsfkhsfkh"
        }
        binding!!.executePendingBindings()
    }

    override fun getItemViewType(position: Int): Int {
        return Math.min(position, 2) //这样让第0和第1个item的类型与其他类型不一样，0是banner，1是grid
    }

    override fun getItemCount(): Int {
        return datas.size + 2
    }

    class DiscoveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class ClickProxy {
        fun gotoPage(view: View?, text: String?) {
            // view.setBackgroundColor(Color.RED);
        }
    }

    init {
        val s = ArrayList<News>()
        s.add(News())
        addNews(s)
    }
}
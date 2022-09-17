package com.ahu.ahutong.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import com.ahu.ahutong.ui.adapter.ImageBannerAdapter.BannerViewHolder
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ahu.ahutong.data.model.Banner
import com.youth.banner.adapter.BannerAdapter
import java.lang.Exception

/**
 * @author simon
 * @date 2022/9/17
 *
 * 参考 https://github.com/youth5201314/banner
 */
class ImageBannerAdapter(data: List<Banner?>?) :
    BannerAdapter<Banner?, BannerViewHolder>(data) {
    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val imageView = ImageView(parent.context)
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        return BannerViewHolder(imageView)
    }

    override fun onBindView(holder: BannerViewHolder?, data: Banner?, position: Int, size: Int) {
        holder?.imageView!!.also {
            it.load(data!!.imageUrl) {
                crossfade(true) //淡入淡出
                crossfade(240)
            }
        }.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data?.detailUrl))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                holder.imageView.context?.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class BannerViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(
        imageView
    )

}
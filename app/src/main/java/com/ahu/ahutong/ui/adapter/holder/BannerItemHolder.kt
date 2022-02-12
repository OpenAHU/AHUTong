package com.ahu.ahutong.ui.adapter.holder

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import coil.load
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.databinding.ItemDiscoveryBannerBinding
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder
import com.ahu.ahutong.ui.widget.banner.BannerView
import com.sink.library.log.SinkLog

class BannerItemHolder(val binding: ItemDiscoveryBannerBinding) :
    BaseViewHolder<ItemDiscoveryBannerBinding, List<Banner>>(binding) {
    override fun bind(data: List<Banner>) {

        binding.itemDiscoveryBanner.setAdapter(object : BannerView.BannerAdapter() {
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
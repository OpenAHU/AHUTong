package com.ahu.ahutong.ui.adapter.holder

import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.databinding.ItemDiscoveryBannerBinding
import com.ahu.ahutong.ui.adapter.ImageBannerAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder
import com.youth.banner.indicator.CircleIndicator

class BannerItemHolder(val binding: ItemDiscoveryBannerBinding) :
    BaseViewHolder<ItemDiscoveryBannerBinding, List<Banner>>(binding) {
    override fun bind(data: List<Banner>) {
        binding.itemDiscoveryBanner.setAdapter(ImageBannerAdapter(data)).indicator = CircleIndicator(binding.root.context);
    }

}
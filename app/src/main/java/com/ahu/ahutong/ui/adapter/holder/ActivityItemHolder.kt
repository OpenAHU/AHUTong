package com.ahu.ahutong.ui.adapter.holder

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.ahu.ahutong.databinding.ItemDiscoveryActivityBinding
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder

class ActivityItemHolder(val binding: ItemDiscoveryActivityBinding) :
    BaseViewHolder<ItemDiscoveryActivityBinding, DiscoveryAdapter.ActivityBean>(binding) {
    override fun bind(data: DiscoveryAdapter.ActivityBean) {
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

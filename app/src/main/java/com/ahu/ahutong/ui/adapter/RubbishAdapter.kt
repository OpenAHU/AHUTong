package com.ahu.ahutong.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Rubbish
import com.ahu.ahutong.databinding.ItemRubbishBinding
import com.ahu.ahutong.databinding.ItemRubbishTypeBinding
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder

/**
 * @Author: SinkDev
 * @Date: 2021/8/24-下午6:18
 * @Email: 468766131@qq.com
 */

class RubbishAdapter(val rubbishes: List<Rubbish>, val type: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class RubbishHolder(val binding: ItemRubbishBinding) :
        BaseViewHolder<ItemRubbishBinding, Rubbish>(binding) {
        override fun bind(data: Rubbish) {
            binding.rubbish = data
        }
    }

    inner class RubbishTypeHolder(val binding: ItemRubbishTypeBinding) :
        BaseViewHolder<ItemRubbishTypeBinding, Rubbish>(binding) {
        override fun bind(data: Rubbish) {
            binding.rubbish = data
        }
    }

    /**
     * 创建Holder
     * @param parent ViewGroup
     * @param viewType Int
     * @return RecyclerView.ViewContainer
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(30, 15, 30, 15)
        return if (type == R.layout.item_rubbish_type) {
            val binding = ItemRubbishTypeBinding.inflate(LayoutInflater.from(parent.context))
            binding.root.layoutParams = layoutParams
            RubbishTypeHolder(binding)
        } else {
            val binding = ItemRubbishBinding.inflate(LayoutInflater.from(parent.context))
            binding.root.layoutParams = layoutParams
            RubbishHolder(binding)
        }
    }

    /**
     * 绑定数据
     * @param holder ViewContainer
     * @param position Int
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RubbishHolder) {
            holder.bind(rubbishes[position])
        } else if (holder is RubbishTypeHolder) {
            holder.bind(rubbishes[position])
        }
    }

    override fun getItemCount(): Int {
        return rubbishes.size
    }

}
package com.ahu.ahutong.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Tool
import com.ahu.ahutong.databinding.ItemMoreBinding
import com.ahu.ahutong.databinding.ItemToolBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder
import com.ahu.ahutong.ui.page.DiscoveryFragment

class MoreAdapter(val bean: MoreBean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemMoreBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_more,
            parent,
            false
        )

       return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ItemHolder
        when(position) {
            0-> {
                itemHolder.binding.sector.text="学习"
                itemHolder.bind(bean.study)
            }
            1-> {
                itemHolder.binding.sector.text="生活"
                itemHolder.bind(bean.life)
            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
    inner class ItemHolder(val binding: ItemMoreBinding) : BaseViewHolder<ItemMoreBinding, List<Tool>>(binding) {
        override fun bind(data: List<Tool>) {
            binding.recycler.layoutManager=GridLayoutManager(binding.root.context, 3)
            binding.recycler.adapter=object : BaseAdapter<Tool, ItemToolBinding>(data) {
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
    }
    data class MoreBean(
        val study: List<Tool>,
        val life: List<Tool>
    )

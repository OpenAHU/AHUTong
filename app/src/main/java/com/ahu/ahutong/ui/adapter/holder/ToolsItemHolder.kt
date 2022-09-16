package com.ahu.ahutong.ui.adapter.holder

import androidx.recyclerview.widget.GridLayoutManager
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Tool
import com.ahu.ahutong.databinding.ItemDiscoveryToolsBinding
import com.ahu.ahutong.databinding.ItemToolBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.adapter.base.BaseViewHolder

class ToolsItemHolder(
    val binding: ItemDiscoveryToolsBinding,
    toolItemSelectAction: ((Tool) -> Unit)
) :
    BaseViewHolder<ItemDiscoveryToolsBinding, List<Tool>>(binding) {
    val toolClickProxy = ToolClickProxy(toolItemSelectAction)
    override fun bind(data: List<Tool>) {
        binding.itemDiscoveryTools.layoutManager = GridLayoutManager(binding.root.context, 5)
        binding.itemDiscoveryTools.adapter = object : BaseAdapter<Tool, ItemToolBinding>(data) {
            override fun layout(): Int {
                return R.layout.item_tool
            }

            override fun bindingData(binding: ItemToolBinding, data: Tool) {
                binding.tool = data
                binding.proxy = toolClickProxy
            }
        }
    }

    inner class ToolClickProxy(val action: ((Tool) -> Unit)) {
        fun onClick(tool: Tool) {
            action(tool)
        }
    }
}

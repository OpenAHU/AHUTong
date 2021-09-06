package com.ahu.ahutong.ui.adapter.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-上午9:02
 * @Email: 468766131@qq.com
 */
abstract class BaseViewHolder<T: ViewDataBinding, DT>(binding: T): RecyclerView.ViewHolder(binding.root){
    abstract fun bind(data: DT)
}
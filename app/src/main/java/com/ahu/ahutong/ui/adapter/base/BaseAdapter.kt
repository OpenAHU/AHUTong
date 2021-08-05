package com.ahu.ahutong.ui.adapter.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


/**
 * @Author: SinkDev
 * @Date: 2021/8/1-上午9:14
 * @Email: 468766131@qq.com
 */
abstract class BaseAdapter<T, VD: ViewDataBinding>(data: List<T> = emptyList()): RecyclerView.Adapter<BaseAdapter<T, VD>.SimpleViewHolder>() {
    private var data: List<T>

    init {
        this.data = data
    }

    abstract fun layout(): Int
    abstract fun bindingData(binding: VD, data: T)
    inner class SimpleViewHolder(val binding: VD): BaseViewHolder<VD, T>(binding){
        override fun bind(data: T) {
            bindingData(binding, data)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val vd = DataBindingUtil.inflate<VD>(LayoutInflater.from(parent.context), layout(), parent, false)
        return SimpleViewHolder(vd)

    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(data: List<T>){
        this.data = data
        notifyDataSetChanged()
    }
}
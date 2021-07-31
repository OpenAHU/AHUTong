package com.sink.library.log.printer.floatview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sink.library.R

/**
 * @Author SinkDev
 * @Date 2021/6/5-13:23
 * @Email 468766131@qq.com
 */
internal class LogAdapter(private val logs: MutableList<Log>) :
    RecyclerView.Adapter<LogAdapter.LogHolder>() {
    inner class LogHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTag: TextView
        val tvLog: TextView

        init {
            tvLog = view.findViewById(R.id.tv_log)
            tvTag = view.findViewById(R.id.tv_tag)
        }

        /**
         * 绑定数据到界面
         * @param log Log
         */
        fun bindTo(log: Log) {
            tvLog.setTextColor(log.color)
            tvTag.setTextColor(log.color)
            tvTag.text = log.formatTag()
            tvLog.text = log.formatLog()
        }
    }

    /**
     * 添加 Log
     * @param log Log
     */
    fun addItem(log: Log) {
        logs.add(log)
        notifyDataSetChanged()
    }

    /**
     * 清除日志
     */
    fun clearItems() {
        logs.clear()
        notifyDataSetChanged()
    }


    /**
     * 获取Logs
     * @return List<Log>
     */
    fun getLogs(): List<Log> {
        return logs.toList();
    }

    override fun onBindViewHolder(holder: LogHolder, position: Int) {
        holder.bindTo(logs[position])
    }

    override fun getItemCount(): Int {
        return logs.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)

        return LogHolder(view)
    }
}
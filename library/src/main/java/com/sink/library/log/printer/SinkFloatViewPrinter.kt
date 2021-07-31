package com.sink.library.log.printer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sink.library.R
import com.sink.library.log.Level
import com.sink.library.log.SinkLogConfig
import com.sink.library.log.SinkLogManager
import com.sink.library.log.printer.floatview.*

/**
 * @Author SinkDev
 * @Date 2021/6/5-13:21
 * @Email 468766131@qq.com
 */
internal class SinkFloatViewPrinter(context: Context) : SinkLogPrinter {
    val logView: View
    val adapter: LogAdapter
    private val floatView: FloatView

    init {
        logView = LayoutInflater.from(context).inflate(R.layout.view_float_log, null, false)
        logView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200)
        val recyclerViewLog = logView.findViewById<RecyclerView>(R.id.recycler_log)
        val tvClose = logView.findViewById<TextView>(R.id.tv_close)
        val tvClear = logView.findViewById<TextView>(R.id.tv_clear)
        adapter = LogAdapter(mutableListOf())
        recyclerViewLog.adapter = adapter
        recyclerViewLog.layoutManager = LinearLayoutManager(context)
        //iconView
        val tvIcon = TextView(context)
        tvIcon.text = context.getString(R.string.text)
        tvIcon.setTextColor(Color.WHITE)
        tvIcon.setPadding(10, 10, 10, 10)
        tvIcon.setBackgroundColor(Color.BLACK)
        val iconView = LinearLayout(context)
        iconView.addView(tvIcon)

        floatView = FloatView(context, iconView, logView)
        //设置事件
        tvClear.setOnClickListener {
            adapter.clearItems()
        }
        tvClose.setOnClickListener {
            floatView.close()
        }
        tvIcon.setOnClickListener {
            floatView.showLog()
        }

        for (log in LogCache.getInstance().allCache){
            adapter.addItem(log)
        }

    }

    override fun print(config: SinkLogConfig, level: Level, tag: String, str: String) {
        adapter.addItem(Log(level, tag, str))

    }

    /**
     * 显示FloatView
     */
    fun show(){
        floatView.show()
    }

    /**
     * 移除Printer
     */
    fun removePrinter(){
        floatView.removeAll()
        SinkLogManager.getInstance().removeLogPrinter(this)
    }
}
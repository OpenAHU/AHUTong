package com.sink.library.log.printer.floatview

import android.graphics.Color
import com.sink.library.log.Level
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author SinkDev
 * @Date 2021/6/5-13:39
 * @Email 468766131@qq.com
 */
internal data class Log(private val level: Level,private val tag: String, private val log: String){
    //Log颜色
    val color: Int = Color.parseColor(level.color)

    val simpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    }

    /**
     * 格式化Tag
     * @return String
     */
    fun formatTag(): String{
        return "${simpleDateFormat.format(Date())} ${level.name[0]}/$tag: "
    }

    fun formatLog(): String{
        return "Log: ${log}"
    }


}

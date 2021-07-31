package com.sink.library.log

import android.util.Log

/**
 * @Author SinkDev
 * @Date 2021/6/4-18:12
 * @Email 468766131@qq.com
 */

enum class Level(val level: Int, val color: String){
    VERBOSE(Log.VERBOSE, "VERBOSE"),
    ERROR(Log.ERROR, "#FF5370"),
    ASSERT(Log.ASSERT, "#FF9492"),
    WARN(Log.WARN, "#F8DA3F"),
    DEBUG(Log.DEBUG, "#54CEE3"),
    INFO(Log.INFO, "#55E350")
}




package com.sink.library.log.printer

import android.util.Log
import com.sink.library.log.Level
import com.sink.library.log.SinkLogConfig

/**
 * @Author SinkDev
 * @Date 2021/6/5-0:49
 * @Email 468766131@qq.com
 */
class SinkLogConsolePrinter : SinkLogPrinter{
    /**
     * Andorid Console 打印
     * @param config SinkLogConfig
     * @param level Level
     * @param tag String
     * @param str String
     */
    override fun print(config: SinkLogConfig, level: Level, tag: String, str: String) {
        Log.println(level.level, tag, str)
    }
}
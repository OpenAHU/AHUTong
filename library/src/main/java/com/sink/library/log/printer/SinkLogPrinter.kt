package com.sink.library.log.printer

import com.sink.library.log.SinkLogConfig
import com.sink.library.log.Level

/**
 * @Author SinkDev
 * @Date 2021/6/5-0:46
 * @Email 468766131@qq.com
 */
interface SinkLogPrinter {
    fun print(config: SinkLogConfig, level: Level, tag: String, str: String)
}
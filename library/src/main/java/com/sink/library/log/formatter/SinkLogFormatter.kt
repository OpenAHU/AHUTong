package com.sink.library.log.formatter

/**
 * @Author SinkDev
 * @Date 2021/6/5-0:50
 * @Email 468766131@qq.com
 */
interface SinkLogFormatter<T> {
    fun format(data: T): String
}
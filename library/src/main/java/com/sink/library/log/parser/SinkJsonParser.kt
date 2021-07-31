package com.sink.library.log.parser

/**
 * @Author SinkDev
 * @Date 2021/6/5-9:55
 * @Email 468766131@qq.com
 */
interface SinkJsonParser{
    fun toJson(obj: Any): String
}

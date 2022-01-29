package com.sink.library.log

import com.sink.library.log.formatter.StackTraceFormatter
import com.sink.library.log.formatter.ThreadFormatter
import com.sink.library.log.parser.SinkJsonParser

/**
 * @Author SinkDev
 * @Date 2021/6/4-21:37
 * @Email 468766131@qq.com
 */
abstract class SinkLogConfig {
    companion object {
        @JvmStatic
        var stackTraceFormatter = StackTraceFormatter()
        @JvmStatic
        var threadFormatter = ThreadFormatter()
    }

    /**
     * 全局默认Tag
     * @return String
     */
    open fun getGlobalTag() = "SinkLog"

    /**
     * 全局开关
     * @return Boolean
     */
    open fun enable() = true

    /**
     * 测试开关
     * @return Boolean
     */
    open fun enableTest() = false
    /**
     * 栈打印深度
     * @return Int
     */
    open fun stackTraceDepth() = 5

    /**
     * 是否包含线程信息
     * @return Boolean
     */
    open fun includeThread() = false

    /**
     * Json解析器
     * @return SinkJsonParser?
     */
    open fun getJsonParser(): SinkJsonParser? = null


}
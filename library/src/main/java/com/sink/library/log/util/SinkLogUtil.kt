package com.sink.library.log.util

/**
 * @Author SinkDev
 * @Date 2021/6/5-11:47
 * @Email 468766131@qq.com
 */

internal fun removeSelfLog(elements: Array<StackTraceElement>, depth: Int, ignorePackageName: String): Array<StackTraceElement>{
    return elements.filter {
        !it.className.contains(ignorePackageName)
    }.take(depth).toTypedArray()
}
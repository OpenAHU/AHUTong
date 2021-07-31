package com.sink.library.log.formatter

import java.lang.StringBuilder

/**
 * @Author SinkDev
 * @Date 2021/6/5-0:53
 * @Email 468766131@qq.com
 */
class StackTraceFormatter : SinkLogFormatter<Array<StackTraceElement>> {
    override fun format(data: Array<StackTraceElement>): String {
        val sb = StringBuilder("")
        var i = 0
        val len = data.size
        while (i < len) {
            if (i == 0) {
                sb.append("StackTrace Log: \n")
            }
            //根据开始结尾 打印不同制表符
            if (i != len - 1) {
                sb.append("\t")
                    .append("├ ")
            }else{
                sb.append("\t")
                    .append("└ ")
            }
            sb.append(data[i].toString())
                .append("\n")
            i++
        }
        return sb.toString()
    }
}
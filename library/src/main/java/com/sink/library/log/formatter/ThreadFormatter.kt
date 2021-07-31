package com.sink.library.log.formatter

/**
 * @Author SinkDev
 * @Date 2021/6/5-0:52
 * @Email 468766131@qq.com
 */
class ThreadFormatter: SinkLogFormatter<Thread> {
    override fun format(data: Thread): String {
        return "Thread: ${data.name}"
    }
}
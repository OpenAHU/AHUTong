package com.sink.library.log

import com.sink.library.log.printer.floatview.Log
import com.sink.library.log.printer.floatview.LogCache
import com.sink.library.log.util.removeSelfLog

/**
 * @Author SinkDev
 * @Date 2021/6/4-18:03
 * @Email 468766131@qq.com
 */

object SinkLog {
    private val PACKAGE_NAME =
        SinkLog.javaClass.name.substring(0, SinkLog.javaClass.name.lastIndexOf('.') + 1)

    /**
     * VERBOSE Log
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun v(vararg contents: Any) {
        vt(SinkLogManager.getInstance().globalConfig.getGlobalTag(), *contents)
    }

    /**
     * VERBOSE Log
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun vt(tag: String, vararg contents: Any) {
        log(Level.VERBOSE, tag, *contents)
    }

    /**
     * DEBUG Log
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun d(vararg contents: Any) {
        dt(SinkLogManager.getInstance().globalConfig.getGlobalTag(), *contents)
    }

    /**
     * DEBUG Log
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun dt(tag: String, vararg contents: Any) {
        log(Level.DEBUG, tag, *contents)
    }

    /**
     * INFO Log
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun i(vararg contents: Any) {
        it(SinkLogManager.getInstance().globalConfig.getGlobalTag(), *contents)
    }

    /**
     * INFO Log
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun it(tag: String, vararg contents: Any) {
        log(Level.INFO, tag, *contents)
    }

    /**
     * WARN Log
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun w(vararg contents: Any) {
        wt(SinkLogManager.getInstance().globalConfig.getGlobalTag(), *contents)
    }

    /**
     * WARN Log
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun wt(tag: String, vararg contents: Any) {
        log(Level.WARN, tag, *contents)
    }

    /**
     * ERROR Log
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun e(vararg contents: Any) {
        et(SinkLogManager.getInstance().globalConfig.getGlobalTag(), *contents)
    }

    /**
     * ERROR Log
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun et(tag: String, vararg contents: Any) {
        log(Level.ERROR, tag, *contents)
    }

    /**
     * ASSERT Log
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun a(vararg contents: Any) {
        at(SinkLogManager.getInstance().globalConfig.getGlobalTag(), *contents)
    }

    /**
     * ASSERT Log
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun at(tag: String, vararg contents: Any) {
        log(Level.ASSERT, tag, *contents)
    }

    /**
     * log method
     * @param level Level
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun log(level: Level, tag: String, vararg contents: Any) {
        log(SinkLogManager.getInstance().globalConfig, level, tag, *contents)
    }

    /**
     * log method
     * @param config SinkLogConfig
     * @param level Level
     * @param tag String
     * @param contents Array<out Any>
     */
    @JvmStatic
    fun log(config: SinkLogConfig, level: Level, tag: String, vararg contents: Any) {
        //判断是否开启
        if (!config.enable()) {
            return
        }
        val sb = StringBuffer()
        //打印内容
        sb.append(parseBody(config, *contents))
            .append("\n")
        //是否打印线程信息
        if (config.includeThread()) {
            sb.append(SinkLogConfig.threadFormatter.format(Thread.currentThread()))
                .append("\n")
        }
        //是否打印调用栈
        if (config.stackTraceDepth() > 0) {
            //去除自身调用栈
            val element =
                removeSelfLog(Throwable().stackTrace, config.stackTraceDepth(), PACKAGE_NAME)
            sb.append(SinkLogConfig.stackTraceFormatter.format(element))
                .append("\n")
        }
        if (SinkLogManager.getInstance().globalConfig.enableTest()) {
            println(sb.toString())
            return
        }
        SinkLogManager.getInstance().printers.forEach {
            LogCache.getInstance().addLog(Log(level, tag, sb.toString()))
            it.print(config, level, tag, sb.toString())
        }

    }


    /**
     * contents序列化为String
     * @param contents Array<out Any>
     * @param config SinkLogConfig
     * @return String
     */
    private fun parseBody(config: SinkLogConfig, vararg contents: Any): String {
        val sb = StringBuffer()
        val jsonParser = config.getJsonParser()
        contents.forEach {
            //根据是否有Json解析器 判断是json 序列化 还是 toString
            sb.append(
                jsonParser?.toJson(it) ?: it.toString()
            ).append(";\t")
        }
        return if (sb.isNotEmpty()) {
            sb.deleteCharAt(sb.length - 1).toString()
        } else {
            sb.toString()
        }
    }


}


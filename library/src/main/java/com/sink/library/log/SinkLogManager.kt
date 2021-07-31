package com.sink.library.log

import android.app.Application
import com.sink.library.log.printer.SinkLogPrinter
import com.sink.library.log.printer.floatview.FloatViewActivityCallback
import java.lang.RuntimeException

/**
 * @Author SinkDev
 * @Date 2021/6/4-21:42
 * @Email 468766131@qq.com
 */
class SinkLogManager private constructor(config: SinkLogConfig, printer: SinkLogPrinter) {
    val globalConfig = config;
    val printers = mutableListOf<SinkLogPrinter>()
    init {
        printers.add(printer)
    }

    /**
     * add LogPrinter
     * @param logPrinter SinkLogPrinter
     */
    fun addLogPrinter(logPrinter: SinkLogPrinter){
        printers.add(logPrinter)
    }

    /**
     * remove LogPrinter
     * @param logPrinter SinkLogPrinter
     */
    fun removeLogPrinter(logPrinter: SinkLogPrinter){
        printers.remove(logPrinter)
    }

    /**
     * 添加FloatLogPrinter
     * @param context Application
     */
    fun addFloatLogPrinter(context: Application){
        context.registerActivityLifecycleCallbacks(FloatViewActivityCallback())
    }

    companion object{
        private lateinit var instance: SinkLogManager
        /**
         * 初始化
         * @param config SinkLogConfig
         */
        @JvmStatic
        fun init(config: SinkLogConfig, printer: SinkLogPrinter){
            if (::instance.isInitialized){
                throw RuntimeException("init() 只能调用一次")
            }
            instance = SinkLogManager(config, printer)
        }

        /**
         * 获取Instance {这个方法必须在init之后调用}
         * @return SinkLogManager
         */
        @JvmStatic
        fun getInstance(): SinkLogManager{
            if (!::instance.isInitialized){
                throw RuntimeException("在此请务必先调用 init() 方法")
            }
            return instance
        }
    }
}
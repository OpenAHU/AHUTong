package com.sink.library.log.printer.floatview

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import com.sink.library.log.SinkLogManager
import com.sink.library.log.printer.SinkFloatViewPrinter

/**
 * @Author SinkDev
 * @Date 2021/7/19-15:10
 * @Email 468766131@qq.com
 */
class FloatViewActivityCallback : ActivityLifecycleCallbacks {
    private lateinit var sinkFloatViewPrinter: SinkFloatViewPrinter

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}


    override fun onActivityResumed(activity: Activity) {
        sinkFloatViewPrinter = SinkFloatViewPrinter(activity)
        SinkLogManager.getInstance().addLogPrinter(sinkFloatViewPrinter)
        sinkFloatViewPrinter.show()
    }

    override fun onActivityPaused(activity: Activity) {
        if (::sinkFloatViewPrinter.isInitialized){
            sinkFloatViewPrinter.removePrinter()
        }
    }



    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
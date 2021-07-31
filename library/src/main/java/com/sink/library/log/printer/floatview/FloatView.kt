package com.sink.library.log.printer.floatview

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager


/**
 * @Author SinkDev
 * @Date 2021/6/5-14:12
 * @Email 468766131@qq.com
 */
internal class FloatView(context: Context, val icon: View, val logView: View) {
    private var logState = false
    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val iconLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
    private val logLayoutParams: WindowManager.LayoutParams

    init {
        iconLayoutParams.format = PixelFormat.RGBA_8888;
        iconLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        iconLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        iconLayoutParams.gravity = Gravity.CENTER or Gravity.END
        iconLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        //LOG
        logLayoutParams = WindowManager.LayoutParams()
        logLayoutParams.format = PixelFormat.RGBA_8888;
        logLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        logLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        logLayoutParams.gravity = Gravity.BOTTOM
        logLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

    }

    /**
     * 显示icon
     */
    fun show(){
        windowManager.addView(icon, iconLayoutParams)
    }

    fun showLog() {
        logState = true
        windowManager.removeView(icon)
        windowManager.addView(logView, logLayoutParams)
    }

    /**
     * 关闭
     */
    fun close(){
        logState = false
        windowManager.removeView(logView)
        windowManager.addView(icon, iconLayoutParams)
    }

    /**
     * 移除所以界面
     */
    fun removeAll(){
        if (logState) {
            windowManager.removeView(logView)
        }else {
            windowManager.removeView(icon)
        }
    }
}
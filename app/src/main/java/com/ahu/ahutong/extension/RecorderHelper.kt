package com.ahu.ahutong.extension

import android.os.Build
import android.view.SurfaceControl
import android.view.Window
import androidx.annotation.RequiresApi

/**
 * Created by tanzhehao on 2025/11/7
 * @author tanzhehao@bytedance.com
 */
internal object RecorderHelper {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun mirrorActivity(source: Window, target: Window) {
        val source: SurfaceControl? = run {
            val vri = source.rootSurfaceControl // as ViewRootImpl
            getSurfaceControlFun.invoke(vri) as SurfaceControl
        }
        val parent: SurfaceControl? = run {
            val vri = target.rootSurfaceControl // as ViewRootImpl
            getSurfaceControlFun.invoke(vri) as SurfaceControl
        }

        val mirror = mirrorSurfaceFun.invoke(null, source) as SurfaceControl

        SurfaceControl.Transaction().reparent(mirror, parent)
            .setLayer(mirror, Int.Companion.MAX_VALUE)
            .setVisibility(mirror, true).apply()
    }
}
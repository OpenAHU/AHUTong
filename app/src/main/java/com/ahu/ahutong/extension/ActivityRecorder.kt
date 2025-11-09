package com.ahu.ahutong.extension

import android.app.Activity
import android.app.Application
import android.app.Presentation
import android.content.res.Resources
import android.hardware.display.DisplayManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.util.Locale
import kotlin.concurrent.thread

/**
 * Created by tanzhehao on 2025/11/9
 * @author tanzhehao@bytedance.com
 */
class ActivityRecorder(
    private val activity: Activity, fps: Int,
    dm: DisplayManager,
    res: Resources
) {
    companion object {
        const val TAG = "ActivityRecorder"
        lateinit var application: Application
        var enableRecord = false

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        fun init(app: Application, enable: Boolean, blockActivities: Set<Class<Activity>>) {
            application = app
            enableRecord = enable
            application.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks by interfaceDelegate() {
                override fun onActivityPostResumed(activity: Activity) {
                    if (activity.javaClass in blockActivities) return
                    attach(activity)
                }

                override fun onActivityPrePaused(activity: Activity) {
                    if (activity.javaClass in blockActivities) return
                    detach(activity)
                }
            })
            thread {
                application.cacheDir.listFiles { file ->
                    file !in recorderSessions
                }?.forEach { file ->
                    // todo upload
                    runCatching { file.delete() }
                }
            }
        }

        private val recorderSessions = mutableListOf<File>()
        private val mapping = mutableMapOf<Activity, ActivityRecorder>()

        private fun attach(activity: Activity) {
            activity.runOnUiThread {
                mapping.getOrPut(activity) {
                    ActivityRecorder(
                        activity,
                        24,
                        activity.getSystemService(DisplayManager::class.java),
                        activity.resources
                    )
                }.start()
            }
        }

        private fun detach(activity: Activity) {
            mapping.remove(activity)?.stop()
        }
    }

    private val name = activity.javaClass.name

    private val screenPair
        get() = activity.resources.displayMetrics.run {
            widthPixels.alignToEven() to heightPixels.alignToEven()
        }


    private val mediaRecorder by lazy {
        MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(screenPair.first, screenPair.second)
            setVideoEncodingBitRate((screenPair.first * screenPair.second * fps * 0.002).toInt())
            setVideoFrameRate(24)
        }
    }
    private val virtualDisplay by lazy {
        dm.createVirtualDisplay(
            name,
            screenPair.first, screenPair.second, res.displayMetrics.densityDpi,
            mediaRecorder.surface,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
        )
    }
    private val presentation by lazy {
        object : Presentation(application, virtualDisplay.display) {
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                window?.decorView?.post {
                    window?.takeIf { enableRecord }?.run {
                        RecorderHelper.mirrorActivity(activity.window, this)
                    }
                }
            }
        }
    }

    fun start() {
        runCatching {
            mediaRecorder.setOutputFile(
                File(
                    application.cacheDir,
                    "${name}-${Locale.getDefault().country}-${System.currentTimeMillis()}.mp4"
                ).apply(recorderSessions::add)
            )
            mediaRecorder.prepare()
            presentation.show()
            mediaRecorder.start()
        }.onFailure {
            Log.e(TAG, "start $name error " + it.message)
        }.onSuccess {
            Log.e(TAG, "start $name")
        }
    }

    fun stop() {
        runCatching {
            presentation.dismiss()
            mediaRecorder.stop()
            mediaRecorder.release()
            virtualDisplay.release()
        }.onFailure {
            Log.e(TAG, "stop $name error " + it.message)
        }.onSuccess {
            Log.e(TAG, "stop $name")
        }
    }
}
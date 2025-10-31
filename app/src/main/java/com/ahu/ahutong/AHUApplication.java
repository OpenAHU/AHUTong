package com.ahu.ahutong;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;


/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */

@HiltAndroidApp
public class AHUApplication extends Application  {

    public volatile static Boolean sessionExpired = true;
    public volatile static Object reLoginMutex = new Object();


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}

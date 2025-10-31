package com.ahu.ahutong;

import arch.sink.BaseApplication;
import dagger.hilt.android.HiltAndroidApp;

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */

@HiltAndroidApp
public class AHUApplication extends BaseApplication {

    public volatile static Boolean sessionExpired = true;
    public volatile static Object reLoginMutex = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
    }

}

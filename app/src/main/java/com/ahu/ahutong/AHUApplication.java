package com.ahu.ahutong;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

import dagger.hilt.android.HiltAndroidApp;


/**
 * @Author Xujiancan
 * @Email 3148336396@qq.com
 */

@HiltAndroidApp
public class AHUApplication extends Application {
    private static Application app;{
        app = this;
    }

    public volatile static Boolean sessionExpired = true;
    public volatile static Object reLoginMutex = new Object();

    public static Application getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "2c2ccadcad", BuildConfig.DEBUG);
    }
}

package com.ahu.ahutong;

import android.app.Activity;
import android.app.Application;
import android.os.Build;

import com.ahu.ahutong.extension.ActivityRecorder;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.HashSet;

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

        // RustSDK add hotUpdate logic
//        com.ahu.ahutong.sdk.RustSDK.INSTANCE.loadLibrary(this);

        CrashReport.initCrashReport(this, "2c2ccadcad", BuildConfig.DEBUG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HashSet<Class<Activity>> blockList = new HashSet<>(){
                // todo LoginScene...
                //  I plan to expose an interface
                //  that allows the business layer to notify [the system/our module] of page switches,
                //  so that corresponding hiding or recording processing can be performed accordingly.
            };
            // todo add privacy related options
            ActivityRecorder.Companion.init(this, true, blockList);
        }
    }
}

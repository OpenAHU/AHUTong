package com.ahu.ahutong;

import com.ahu.ahutong.common.SingleLiveEvent;
import com.tencent.bugly.crashreport.CrashReport;

import arch.sink.BaseApplication;
import kotlin.Unit;

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
public class AHUApplication extends BaseApplication {
    public static SingleLiveEvent<Unit> sessionUpdated = new SingleLiveEvent<>();

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "24521a5b56", BuildConfig.DEBUG);
    }

}

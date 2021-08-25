package com.ahu.ahutong;

import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.sink.library.log.SinkLogConfig;
import com.sink.library.log.SinkLogManager;
import com.sink.library.log.parser.SinkJsonParser;
import com.sink.library.log.printer.SinkLogConsolePrinter;
import com.sink.library.update.CookApkUpdate;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;

import arch.sink.BaseApplication;

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
public class AHUApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //SinkLog
        SinkLogManager.init(new SinkLogConfig() {
            @NotNull
            @Override
            public String getGlobalTag() {
                return "AHUTong";
            }

            @Override
            public boolean enable() {
                return true;
            }

            @Override
            public int stackTraceDepth() {
                return 5;
            }

            @Override
            public boolean includeThread() {
                return true;
            }

            @Override
            public @NotNull SinkJsonParser getJsonParser() {
                return obj -> new Gson().toJson(obj);
            }
        }, new SinkLogConsolePrinter());

        //禁止获取手机ID信息
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setDeviceID("fake-id");
        CrashReport.initCrashReport(this, "24521a5b56", BuildConfig.DEBUG, strategy);
        //初始化更新
        try {
            CookApkUpdate.init(this);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}

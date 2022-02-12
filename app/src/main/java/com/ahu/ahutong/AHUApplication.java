package com.ahu.ahutong;

import android.content.pm.PackageManager;

import com.ahu.plugin.BathPlug;
import com.ahu.plugin.BathPlugImpl;
import com.google.gson.Gson;
import com.sink.library.log.SinkLogConfig;
import com.sink.library.log.SinkLogManager;
import com.sink.library.log.parser.SinkJsonParser;
import com.sink.library.log.printer.SinkLogConsolePrinter;
import com.tencent.bugly.crashreport.CrashReport;
import org.jetbrains.annotations.NotNull;
import arch.sink.BaseApplication;

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
public class AHUApplication extends BaseApplication {
    public static int width, height;


    @Override
    public void onCreate() {
        super.onCreate();
        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;
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

    }
    private static final BathPlugImpl bath=new BathPlugImpl();

    /**
     * 返回浴室开放计算类
     * @return 类
     */
    public static BathPlug getBath() {
        return bath;
    }
}

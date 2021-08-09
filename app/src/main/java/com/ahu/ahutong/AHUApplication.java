package com.ahu.ahutong;

import com.google.gson.Gson;
import com.sink.library.log.SinkLogConfig;
import com.sink.library.log.SinkLogManager;
import com.sink.library.log.parser.SinkJsonParser;
import com.sink.library.log.printer.SinkLogConsolePrinter;

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
    }
}

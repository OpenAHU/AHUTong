package com.sink.library.log.printer.floatview;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author SinkDev
 * @Date 2021/7/19-15:52
 * @Email 468766131@qq.com
 */
public class LogCache {
    private static List<Log> cacheList;
    private static final LogCache LOG_CACHE = new LogCache();
    private LogCache(){
        cacheList = new ArrayList<>();
    }
    public static LogCache getInstance(){
        return LOG_CACHE;
    }

    public void addLog(@NonNull Log log){
        cacheList.add(log);
    }

    @NonNull
    public Log[] getAllCache(){
        return cacheList.toArray(new Log[0]);
    }


}

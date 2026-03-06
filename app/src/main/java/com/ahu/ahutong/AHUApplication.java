package com.ahu.ahutong;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.ahu.ahutong.sdk.LocalServiceClient;
import com.ahu.ahutong.sdk.RustSDK;
import com.tencent.bugly.crashreport.CrashReport;
import com.ahu.ahutong.data.AHURepository;
import com.ahu.ahutong.data.dao.AHUCache;

import org.json.JSONObject;

import java.util.HashSet;

import dagger.hilt.android.HiltAndroidApp;

/**
 * @Author Xujiancan
 * @Email 3148336396@qq.com
 */

@HiltAndroidApp
public class AHUApplication extends Application {
    private static final String TAG = "AHUApplication";

    private static Application app;
    {
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

        // 初始化数据源（根据 Mock 开关）
        if(AHUCache.INSTANCE.getMockData()){
            AHURepository.INSTANCE.initializeDataSource(true);
            Toast.makeText(this,"正在使用mock数据",Toast.LENGTH_SHORT).show();
        }


        // 注意: Local Service 在 MainActivity.init() 中启动（native library 加载后）

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HashSet<Class<Activity>> blockList = new HashSet<>() {
                // todo LoginScene...
                // I plan to expose an interface
                // that allows the business layer to notify [the system/our module] of page
                // switches,
                // so that corresponding hiding or recording processing can be performed
                // accordingly.
            };
            // todo add privacy related options
        }
    }

    /**
     * 启动 Rust 本地 HTTP 服务
     */
    private void startLocalService() {
        // 确保 native library 已加载
        if (!RustSDK.INSTANCE.isNativeLoaded()) {
            Log.w(TAG, "Native library not loaded yet, skipping local service start");
            return;
        }

        try {
            // 启动服务，端口 0 表示随机分配
            String result = RustSDK.INSTANCE.startServer(0);
            Log.i(TAG, "startServer result: " + result);

            if (result.contains("\"error\"")) {
                Log.e(TAG, "Failed to start local server: " + result);
                return;
            }

            // 解析返回的 port 和 token
            JSONObject json = new JSONObject(result);
            int port = json.getInt("port");
            String token = json.getString("token");

            // 初始化 LocalServiceClient 单例
            LocalServiceClient.Companion.initialize(port, token);

            Log.i(TAG, "Local service started successfully on port: " + port);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start local service", e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // 停止 Rust 本地服务
        try {
            if (RustSDK.INSTANCE.isNativeLoaded()) {
                RustSDK.INSTANCE.stopServer();
                LocalServiceClient.Companion.destroy();
                Log.i(TAG, "Local service stopped");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop local service", e);
        }
    }
}

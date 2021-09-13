package com.ahu.ahutong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Looper;
import android.widget.Toast;

import com.ahu.plugin.BathPlug;
import com.ahu.plugin.BathPlugImpl;
import com.ahu.plugin.PlugLoader;
import com.google.gson.Gson;
import com.simon.library.AppUpdate;
import com.sink.library.log.SinkLogConfig;
import com.sink.library.log.SinkLogManager;
import com.sink.library.log.parser.SinkJsonParser;
import com.sink.library.log.printer.SinkLogConsolePrinter;
import com.sink.library.update.CookApkUpdate;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import arch.sink.BaseApplication;

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
public class AHUApplication extends BaseApplication implements AppUpdate.CallBack {
    public static int width, height;
    private static BathPlug bathPlug;
    public static String version;
    public static String plugPath;

    /**
     * 获取浴室开放数据插件
     * @return 插件对象
     */
    public static BathPlug getBathPlug(){
        return bathPlug==null?bathPlug=new BathPlugImpl():bathPlug;
    }

    /**
     * 设置浴室开放插件
     * @param plug 插件对象
     */
    public static void setBathPlug(BathPlug plug){
        bathPlug=plug;
    }

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
        try {
            version = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            plugPath=getNoBackupFilesDir().toString()+ File.separator+"plug.dex";
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("这错误够离谱的");
        }
        try {
            PlugLoader.init(this);
            PlugLoader.load(plugPath);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        AppUpdate.check(version,getBathPlug().getVersion(),plugPath,this);
        //初始化更新
//        try {
//            CookApkUpdate.init(this);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void plugUpdate(Object plug) {
        setBathPlug((BathPlug) plug);
    }

    @Override
    public void appUpdate(String url, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发现新版本");
        builder.setMessage("新版特性：\n"+msg);
        builder.setPositiveButton("更新", (dialog, which) -> {
            Intent intent= new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
            dialog.dismiss();
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void requestError(Exception e) {
        Looper.prepare();
        Toast.makeText(this,"检查更新出错"+e.getMessage(),Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onLatestVersion() { }
}

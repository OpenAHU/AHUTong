package com.sink.library.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.sink.library.update.bean.App;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;


/**
 * @Author: SinkDev
 * @Date: 2021/8/25-上午11:37
 * @Email: 468766131@qq.com
 */
public class CookApkUpdate {
    private static String versionName;
    private static String packageName;
    private final static String COOL_APK_URL = "https://www.coolapk.com/apk/%s";

    private static Handler handler;

    public static void init(Context context) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        packageName = context.getPackageName();
        PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
        versionName = packageInfo.versionName;
        handler = new Handler(Looper.getMainLooper());
    }


    public static synchronized void checkUpdate(UpdateListener listener) {
        if (handler == null) {
            throw new IllegalStateException("必须先init");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection.Response response = Jsoup.newSession()
                            .url(String.format(COOL_APK_URL, packageName))
                            .method(Connection.Method.GET)
                            .timeout(5000)
                            .execute();
                    Map<String, String> cookies = response.cookies();
                    Element body = response.parse().body();
                    String serverVersionName = body.select("span.list_app_info").text();
                    if (versionName.equals(serverVersionName)) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onLatestVersion();
                            }
                        });
                        return;
                    }
                    String name = body.select("p.detail_app_title").get(0).ownText();
                    Elements elements = body.select("p.apk_left_title_nav");
                    String info = "暂无更新日志.";
                    for (Element element : elements) {
                        if ("新版特性".equals(element.text())) {
                            Element parent = element.parent();
                            if (parent == null) {
                                throw new IllegalStateException("未知错误，请重试。");
                            }
                            info = parent.select("p.apk_left_title_info").html()
                                    .replace("<br>", "\n");
                            break;
                        }
                    }
                    String url = body.select("a.show-dialog").attr("href");
                    response = Jsoup.newSession()
                            .url(url)
                            .followRedirects(false)
                            .cookies(cookies)
                            .method(Connection.Method.GET)
                            .execute();
                    String apkUrl = response.header("Location");
                    App app = new App();
                    app.setName(name);
                    app.setApkUrl(apkUrl);
                    app.setIntro(info);
                    app.setVersionName(serverVersionName);
                    app.setCookApkUrl(String.format(COOL_APK_URL, packageName));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onNeedUpdate(app);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.checkFailure(e);
                        }
                    });

                }
            }
        }).start();

    }

    interface UpdateListener {
        void onNeedUpdate(App app);

        void onLatestVersion();

        void checkFailure(Throwable throwable);
    }

}

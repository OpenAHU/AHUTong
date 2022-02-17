package com.ahu.ahutong.data.reptile.utils;

import com.ahu.ahutong.data.reptile.ReptileManager;
import com.sink.library.log.SinkLog;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class JsoupProxy {

    private static final Map<Integer, String> urlCache = new HashMap<>();

    public static Connection newSession() {
        Connection realConnect = new HttpConnection();
        return (Connection) Proxy.newProxyInstance(JsoupProxy.class.getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (!ReptileManager.getInstance().isWVPN()) {
                    if ("url".equals(method.getName()) && args[0] instanceof String) {
                        args[0] = getPlaintUrl((String) args[0]);
                    }
                    if ("referrer".equals(method.getName()) && args[0] instanceof String) {
                        args[0] = getPlaintUrl((String) args[0]);
                    }

                }
                return method.invoke(realConnect, args);
            }
        });
    }

    private static String getPlaintUrl(String proxyUrl) throws Exception {
        if (!urlCache.containsKey(proxyUrl.hashCode())) {
            urlCache.put(proxyUrl.hashCode(),  VpnURL.getPlaintUrl(proxyUrl));
        }
        return urlCache.get(proxyUrl.hashCode());
    }
}

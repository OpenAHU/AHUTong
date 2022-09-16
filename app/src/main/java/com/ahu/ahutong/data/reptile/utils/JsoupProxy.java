package com.ahu.ahutong.data.reptile.utils;

import org.jsoup.Connection;

import java.util.HashMap;
import java.util.Map;

public class JsoupProxy {

    private static final Map<Integer, String> urlCache = new HashMap<>();

    public static Connection newSession() {
//        Connection realConnect = new HttpConnection();
//        return (Connection) Proxy.newProxyInstance(JsoupProxy.class.getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
//            @Override
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                if (!ReptileManager.getInstance().isWVPN()) {
//                    if ("url".equals(method.getName()) && args[0] instanceof String) {
//                        args[0] = getPlaintUrl((String) args[0]);
//                    }
//                    if ("referrer".equals(method.getName()) && args[0] instanceof String) {
//                        args[0] = getPlaintUrl((String) args[0]);
//                        Log.e("SINK", "referrer = " + args[0].toString());
//                    }
//
//                }
//                Log.e("SINK", method.getName());
//                return method.invoke(realConnect, args);
//            }
//        });
        return new SinkHttpConnection();
    }

    private static String getPlaintUrl(String proxyUrl) throws Exception {
        if (!urlCache.containsKey(proxyUrl.hashCode())) {
            urlCache.put(proxyUrl.hashCode(), VpnURL.getPlaintUrl(proxyUrl));
        }
        return urlCache.get(proxyUrl.hashCode());
    }
}

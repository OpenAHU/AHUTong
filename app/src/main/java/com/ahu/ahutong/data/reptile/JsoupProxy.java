package com.ahu.ahutong.data.reptile;

import com.ahu.ahutong.data.reptile.utils.VpnURL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JsoupProxy {
    public static Connection newSession() {
        Connection connection = Jsoup.newSession();
        return (Connection) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                if ("url".equals(method.getName()) && objects[0] instanceof String) {
                    objects[0] = VpnURL.getProxyUrl((String) objects[0]);
                }
                return method.invoke(connection, objects);
            }
        });
    }
}

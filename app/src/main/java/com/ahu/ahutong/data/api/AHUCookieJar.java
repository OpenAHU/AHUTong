package com.ahu.ahutong.data.api;


import android.util.Log;

import androidx.annotation.NonNull;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.cache.CookieCache;
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

// Warning: 别动， 用于修复后端非法 Cookie 被过滤没有保存问题
public class AHUCookieJar implements ClearableCookieJar {
    private static final String TAG = AHUCookieJar.class.getName();
    private final CookieCache cache;
    private final CookiePersistor persistor;

    public AHUCookieJar(CookieCache cache, CookiePersistor persistor) {
        this.cache = cache;
        this.persistor = persistor;

        this.cache.addAll(persistor.loadAll());
    }

    @Override
    synchronized public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        Log.i(TAG, "saveFromResponse: " + url);
        cache.addAll(cookies);
        persistor.saveAll(filterPersistentCookies(cookies));
    }

    private static List<Cookie> filterPersistentCookies(List<Cookie> cookies) {
        List<Cookie> persistentCookies = new ArrayList<>();

        for (Cookie cookie : cookies) {
            Log.i(TAG, "persistent: " + cookie.persistent());
            persistentCookies.add(cookie);

        }
        return persistentCookies;
    }

    @NonNull
    @Override
    synchronized public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        Log.i(TAG, "loadForRequest: " + url);
        List<Cookie> cookiesToRemove = new ArrayList<>();
        List<Cookie> validCookies = new ArrayList<>();

        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();

            if (isCookieExpired(currentCookie)) {
                cookiesToRemove.add(currentCookie);
                it.remove();

            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

        persistor.removeAll(cookiesToRemove);
        Log.i(TAG, "validCookies: " + validCookies);
        return validCookies;
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    @Override
    synchronized public void clearSession() {
        Log.i(TAG, "clearSession");
        cache.clear();
        cache.addAll(persistor.loadAll());
    }

    @Override
    synchronized public void clear() {
        cache.clear();
        persistor.clear();
    }


    public void logAllCookies() {
        Log.i(TAG, "=== 当前 CookieJar 中的所有 cookie ===");
        for (Cookie cookie : cache) {
            Log.i(TAG, String.format(
                    "name=%s; value=%s; domain=%s; path=%s; secure=%b; httponly=%b; expiresAt=%s",
                    cookie.name(),
                    cookie.value(),
                    cookie.domain(),
                    cookie.path(),
                    cookie.secure(),
                    cookie.httpOnly(),
                    new java.util.Date(cookie.expiresAt()).toString()
            ));
        }
        Log.i(TAG, "=== End of Cookie list ===");
    }


    public void clearCookiesForUrl(@NonNull String url) {

        HttpUrl urlToDelete = HttpUrl.get(url);

        Log.i(TAG, "clearCookiesForUrl: " + url);
        List<Cookie> cookiesToRemove = new ArrayList<>();

        synchronized (this) {
            for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
                Cookie cookie = it.next();

                if (cookie.matches(urlToDelete)) {
                    cookiesToRemove.add(cookie);
                    it.remove();
                    Log.i(TAG, "Removed cookie: " + cookie);
                }
            }

            // 同步更新持久化
            if (!cookiesToRemove.isEmpty()) {
                persistor.removeAll(cookiesToRemove);
            }
        }
    }
}

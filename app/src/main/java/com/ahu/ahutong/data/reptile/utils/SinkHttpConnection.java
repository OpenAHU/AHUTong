package com.ahu.ahutong.data.reptile.utils;

import androidx.annotation.NonNull;

import com.ahu.ahutong.data.reptile.ReptileManager;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.util.HashMap;
import java.util.Map;

public class SinkHttpConnection extends HttpConnection {
    private static final Map<Integer, String> urlCache = new HashMap<>();

    private static String getPlaintUrl(String proxyUrl) throws Exception {
        if (!urlCache.containsKey(proxyUrl.hashCode())) {
            urlCache.put(proxyUrl.hashCode(), VpnURL.getPlaintUrl(proxyUrl));
        }
        return urlCache.get(proxyUrl.hashCode());
    }

    @NonNull
    @Override
    public Connection url(@NonNull String url) {
        if (!ReptileManager.getInstance().isWVPN()) {
            try {
                url = getPlaintUrl(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.url(url);
    }

    @NonNull
    @Override
    public Connection referrer(@NonNull String referrer) {
        if (!ReptileManager.getInstance().isWVPN()) {
            try {
                referrer = getPlaintUrl(referrer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.referrer(referrer);
    }
}

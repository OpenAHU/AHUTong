package com.ahu.ahutong.data.reptile.store;

import java.util.Map;

public interface CookieStore {
    void put(String name, String value);

    void putAll(Map<String, String> cookies);

    String get(String name);

    void remove(String name);

    void removeAll();

    Map<String, String> getCookies();
}

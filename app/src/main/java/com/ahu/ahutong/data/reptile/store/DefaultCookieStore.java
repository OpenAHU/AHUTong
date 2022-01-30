package com.ahu.ahutong.data.reptile.store;

import java.util.*;

public class DefaultCookieStore implements CookieStore {
    Map<String, String> cookieMap = new HashMap<>();
    @Override
    public void put(String name, String value) {
        System.out.println("name = " + name + ", value = " + value);
        cookieMap.put(name, value);
    }

    @Override
    public void putAll(Map<String, String> cookies) {

        cookieMap.putAll(cookies);
        if (cookieMap.containsKey("refresh")){
            cookieMap.put("refresh", "0");
        }
        System.out.println("cookies = " + cookieMap);
    }

    @Override
    public String get(String name) {
        return cookieMap.get(name);
    }

    @Override
    public void remove(String name) {
        cookieMap.remove(name);
    }

    @Override
    public void removeAll() {
        cookieMap.clear();
    }

    @Override
    public Map<String, String> getCookies() {

        return this.cookieMap;
    }

}

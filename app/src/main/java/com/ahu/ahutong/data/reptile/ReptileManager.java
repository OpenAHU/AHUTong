package com.ahu.ahutong.data.reptile;


import android.webkit.CookieManager;

import com.ahu.ahutong.data.reptile.store.CookieStore;

/**
 * @author sink
 */
public class ReptileManager {
    private static final ReptileManager INSTANCE = new ReptileManager();
    private CookieStore store;
    private ReptileUser currentReptileUser;
    private int timeout = 5000;
    private boolean loginStatus = false;
    private boolean isWVPN = true;

    private ReptileManager() {
    }

    public static ReptileManager getInstance() {
        return INSTANCE;
    }

    public CookieStore getCookieStore() {
        return store;
    }

    public ReptileManager setCookieStore(CookieStore store) {
        this.store = store;
        return this;
    }

    public ReptileManager setCurrentUser(String username, String password) {
        this.currentReptileUser = new ReptileUser(username, password);
        return this;
    }

    public ReptileUser getCurrentUser() {
        return currentReptileUser;
    }


    public int getTimeout() {
        return timeout;
    }

    public ReptileManager setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public boolean isLoginStatus() {
        return loginStatus;
    }

    public ReptileManager setLoginStatus(boolean status) {
        loginStatus = status;
        return this;
    }

    public boolean isWVPN() {
        return isWVPN;
    }

    public ReptileManager setWVPN(boolean WVPN) {
        isWVPN = WVPN;
        return this;
    }

    public String getCookie(String url) {
        CookieManager instance = CookieManager.getInstance();
        if (isWVPN) {
            return instance.getCookie(Constants.URL_LOGIN_BASE);
        } else {
            return instance.getCookie(url);
        }
    }
}

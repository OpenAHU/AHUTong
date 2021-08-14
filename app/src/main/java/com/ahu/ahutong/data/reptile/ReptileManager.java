package com.ahu.ahutong.data.reptile;


import com.ahu.ahutong.data.reptile.store.CookieStore;

/**
 * @author sink
 */
public class ReptileManager {
    private static final ReptileManager INSTANCE = new ReptileManager();
    private CookieStore store;
    private ReptileUser currentReptileUser;
    private int timeout = 5000;

    private ReptileManager(){}

    public static ReptileManager getInstance() {
        return INSTANCE;
    }

    public ReptileManager setCookieStore(CookieStore store) {
        this.store = store;
        return this;
    }

    public CookieStore getCookieStore(){
        return store;
    }

    public ReptileManager setCurrentUser(String username, String password){
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
}

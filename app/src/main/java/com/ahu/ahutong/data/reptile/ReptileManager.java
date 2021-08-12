package com.ahu.ahutong.data.reptile;


import com.ahu.ahutong.data.reptile.store.CookieStore;

public class ReptileManager {
    private static final ReptileManager INSTANCE = new ReptileManager();
    private CookieStore store;
    private User currentUser;
    private int timeout = 5000;

    private ReptileManager(){}

    public static ReptileManager getInstance() {
        return INSTANCE;
    }

    public void setCookieStore(CookieStore store) {
        this.store = store;
    }

    public CookieStore getCookieStore(){
        return store;
    }

    public void setCurrentUser(String username, String password){
        this.currentUser = new User(username, password);
    }

    public User getCurrentUser() {
        return currentUser;
    }


    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

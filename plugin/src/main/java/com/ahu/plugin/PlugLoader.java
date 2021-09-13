package com.ahu.plugin;

import android.annotation.SuppressLint;
import android.content.Context;

import dalvik.system.DexClassLoader;

public class PlugLoader {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    public static void init(Context context){
        mContext =context;
    }
    public static Object load(String path) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        DexClassLoader dcl=new DexClassLoader(path, mContext.getCacheDir().getPath(), null, mContext.getClassLoader());
        Class<?> pluginCls = dcl.loadClass("com.ahu.plugin.BathPlugImpl");
        return pluginCls.newInstance();
    }
}

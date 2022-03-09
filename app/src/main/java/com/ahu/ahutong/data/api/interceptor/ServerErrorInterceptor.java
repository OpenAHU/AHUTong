package com.ahu.ahutong.data.api.interceptor;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ahu.ahutong.AHUApplication;
import com.ahu.ahutong.data.dao.AHUCache;
import com.ahu.ahutong.data.model.User;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ServerErrorInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        Log.e("ServerErrorInterceptor", response.toString());
        if (response.code() == 400) {
            // token 过期
            AHUCache.INSTANCE.clearCurrentUser();
            AHUCache.INSTANCE.saveWisdomPassword("");
        }
        if (!response.isSuccessful()) {
            // 后端服务异常, 切换本地服务
            AHUApplication.loginType.setValue(User.UserType.AHU_LOCAL);
        }
        return response;
    }
}

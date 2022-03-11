package com.ahu.ahutong.data.api.interceptor;


import android.util.Log;

import androidx.annotation.NonNull;

import com.ahu.ahutong.AHUApplication;
import com.ahu.ahutong.data.model.User;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

public class ServerErrorInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (response.code() == 400) {
            // token 过期
            AHUApplication.retryLogin.callFromOtherThread();
        }
        return response;
    }
}

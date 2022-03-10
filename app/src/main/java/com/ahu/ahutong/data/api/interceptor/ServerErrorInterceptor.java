package com.ahu.ahutong.data.api.interceptor;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ahu.ahutong.AHUApplication;
import com.ahu.ahutong.data.dao.AHUCache;
import com.ahu.ahutong.data.model.User;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ServerErrorInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response;
        try {
            response = chain.proceed(chain.request());
        } catch (IOException e) {
            e.printStackTrace();
            return new Response.Builder()
                    .code(500)
                    .message("请求超时，服务器或网络异常，请稍后再试！")
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .body(ResponseBody.create("{}", MediaType.parse("application/json")))
                    .build();
        }
        if (response.code() == 400) {
            // token 过期
            AHUApplication.retryLogin.callFromOtherThread();
        } else if (!response.isSuccessful()) {
            // 后端服务异常, 切换本地服务
            AHUApplication.loginType.setValue(User.UserType.AHU_LOCAL);
        }
        return response;
    }
}

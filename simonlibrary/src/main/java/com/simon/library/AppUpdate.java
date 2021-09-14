package com.simon.library;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdate {
    public interface CallBack{
        void appUpdate(String url,String msg);
        void requestError(Exception e);
        void onLatestVersion();
    }

    /**
     * 检查版本更新
     * @param version 当前app版本
     * @param cb 数据回调
     */
    public static void check(String version,CallBack cb){
        new Thread(()->{
            try {
                HttpURLConnection connection= (HttpURLConnection) new URL("https://ahuer.cn/api/android/version").openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                    String result=streamToString(inputStream);
                    Log.e("update","result:"+result);
                    JSONObject jsonObject=new JSONObject(result);//code为0代表请求成功，-1是业务异常，-2是系统异常
                    int code=jsonObject.optInt("code",-1);
                    String msg= jsonObject.optString("msg");
                    switch (code){
                        case 0:
                            jsonObject=jsonObject.getJSONObject("data");
                            if (!jsonObject.optString("version").equals(version)){
                                msg=jsonObject.optString("msg");
                                String url=jsonObject.optString("url");
                                Log.e("update","appUpdate,url"+url+",msg+"+msg);
                                cb.appUpdate(url,msg);
                            }else{
                                Log.e("update","onLatestVersion");
                                cb.onLatestVersion();
                            }
                            break;
                        case -1:
                            cb.requestError(new IOException("检查更新出错，业务异常"+msg));
                            break;
                        case -2:
                            cb.requestError(new IOException("检查更新出错，系统异常"+msg));
                            break;
                    }
                }else {
                    cb.requestError(new IOException("检查更新出现未知错误,请求"));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                cb.requestError(e);
            }
        }).start();
    }


    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return 数据
     */
    private static String streamToString(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int i;
        while ((i = is.read(buffer)) != -1) {
            bos.write(buffer, 0, i);
        }
        bos.close();
        is.close();
        return bos.toString();
    }
}

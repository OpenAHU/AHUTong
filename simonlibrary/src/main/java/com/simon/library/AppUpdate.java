package com.simon.library;

import com.ahu.plugin.PlugLoader;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdate {
    public static abstract interface CallBack{
        void plugUpdate(Object plug);
        void appUpdate(String url,String msg);
        void requestError(Exception e);
        void onLatestVersion();
    }

    /**
     * 检查版本更新
     * @param version 当前app版本
     * @param plugVersion 当前插件版本
     * @param cb 数据回调
     */
    public static void check(String version,String plugVersion,String plugPath,CallBack cb){
        new Thread(()->{
            try {
                HttpURLConnection connection= (HttpURLConnection) new URL("").openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                    String result=streamToString(inputStream);
                    JSONObject jsonObject=new JSONObject(result);
                   if (200==jsonObject.optInt("code",0)){
                       jsonObject=jsonObject.getJSONObject("data");
                       if (!jsonObject.optString("version").equals(version)){
                           String msg=jsonObject.optString("msg");
                           String url=jsonObject.optString("url");
                           cb.appUpdate(url,msg);
                       }else
                           cb.onLatestVersion();
                       if (!jsonObject.optString("plugVersion").equals(plugVersion)){
                           String url=jsonObject.optString("plugUrl");
                           Object plug=downLoadPlug(url,new File(plugPath));
                           cb.plugUpdate(plug);
                       }
                   }
                }
            } catch (IOException | JSONException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                cb.requestError(e);
            }
        }).start();
    }

    /**
     * 下载插件对象
     * @param url 下载链接
     * @return 对象
     */
    private static Object downLoadPlug(String url,File plug) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
            InputStream in = new URL(url).openConnection().getInputStream();
            FileOutputStream fo = new FileOutputStream(plug);
            byte[] buffer = new byte[1024 * 1024];
            int len;
            while( (len = in.read(buffer)) > 0)
                fo.write(buffer, 0, len);
            in.close();
            fo.close();
       return PlugLoader.load(plug.getAbsolutePath());
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

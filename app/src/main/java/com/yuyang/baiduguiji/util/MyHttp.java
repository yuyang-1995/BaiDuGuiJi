package com.yuyang.baiduguiji.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MyHttp {

    public static void sendRequestOkHttpForGet(final String adress,final MyCallBack myCallBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(adress);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    String response = convertStreamToString(in);
                    //回调接口函数，让主线程处理
                    //成功
                    myCallBack.onResponse(response);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    //失败
                    myCallBack.onFailure(e);
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    myCallBack.onFailure(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    myCallBack.onFailure(e);
                } finally {
                    if (null != connection) {
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}

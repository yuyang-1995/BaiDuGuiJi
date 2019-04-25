package com.yuyang.baiduguiji.application;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.util.BringData;
import com.yuyang.baiduguiji.util.SaveKeyValues;

import java.io.IOException;

/**
 * Author: yuyang
 * Date:2018/12/15 17:36
 */

/**
 * Application 实例创建时调用
 *Android系统的入口是Application类的 onCreate（），默认为空实现作用
 *
 * 初始化 应用程序级别 的资源，如全局对象、环境配置变量、图片资源初始化、推送服务的注册等
 *注：请不要执行耗时操作，否则会拖慢应用程序启动速度
 *数据共享、数据缓存
 * 设置全局共享数据，如全局共享变量、方法等
 * 注：这些共享数据只在应用程序的生命周期内有效，当该应用程序被杀死，这些数据也会被清空，所以只能存储一些具备 临时性的共享数据
 *
 */
public class DemoApplication extends Application {

    public static Bitmap[] bitmaps = new Bitmap[5];
    public static String[] shuoming = new String[5];
    @Override
    public void onCreate() {
        super.onCreate();
        //定位
      //  new GetLocation(getApplicationContext());
        //实例化sharedPreferences
        SaveKeyValues.createSharePreferences(this);
        bitmaps[0] = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.mrkj_fushen1);
        bitmaps[1] = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.mipmap.mrkj_fuwocheng1);
        bitmaps[2] = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.mipmap.mrkj_gunlun1);
        bitmaps[3] = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.mipmap.mrkj_wotui1);
        bitmaps[4] = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.mipmap.mrkj_sanwanju1);

        shuoming[0] = "俯身哑铃飞鸟";
        shuoming[1] = "俯卧撑";
        shuoming[2] = "滚轮支点俯卧撑";
        shuoming[3] = "平板卧推";
        shuoming[4] = "仰卧平板杠铃肱三弯举";
        //将食物热量
        int saveDateIndex = SaveKeyValues.getIntValues("date_index",0);
        Log.e("数据库数否被存入", "【" + saveDateIndex + "】");
        if (saveDateIndex == 0){
            try {
                SaveKeyValues.putIntValues("date_index", 1);
                BringData.getDataFromAssets(getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.yuyang.baiduguiji.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.yuyang.baiduguiji.database.DatasDao;
import com.yuyang.baiduguiji.receiver.FunctionBroadcastReceiver;
import com.yuyang.baiduguiji.util.DateUtils;
import com.yuyang.baiduguiji.util.SaveKeyValues;
import com.yuyang.baiduguiji.util.StepDetector;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * 记录保存服务(此处用于记录值)
 */
public class RecordedSaveService extends Service {
    //
    public static final String cancelSaveService = "mrkj.healthylife.RECORDED";

    //需要用到SQLite数据库
    private DatasDao datasDao;

    public RecordedSaveService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("测试服务","启动了！");
        //获取 用户步长和重量值
        int custom_step_length = SaveKeyValues.getIntValues("length",70);
        int custom_weight = SaveKeyValues.getIntValues("weight", 50);
        //创建数据库工具类
        datasDao = new DatasDao(this);

        //判断活动是否在运行
        boolean result = isActivityRunning(this);
        int step;

        //判断如何存储数值， 如果活动在运行 获取活动运行时增加的步数  如果活动killed 则将运动中的步数 加上 sharedPreferences 的已有值
        if (result){
            //获取步数直接获取
            step = StepDetector.CURRENT_SETP;
        }else {
            //获取xml存值加记录步数
            step = StepDetector.CURRENT_SETP + SaveKeyValues.getIntValues("sport_steps" ,0);
        }
        //通过步数计算消耗的热量和行走的路程
        double distance_values = step * custom_step_length * 0.01 *0.001;//km
        String distance_Str = formatDouble(distance_values);

        double heat_values = custom_weight * distance_values * 1.036;//cls
        String heat_Str = formatDouble(heat_values);

        //从DateUtils中获取日期  Object 是所有类的父类
        Map<String,Object> map = DateUtils.getDate();
        int year = (int) map.get("year");
        int month = (int) map.get("month");
        int day = (int) map.get("day");
        String date = (String) map.get("date");

        //存入数据
        //用ContntValues 存储数据
        ContentValues values = new ContentValues();
        values.put("date",date);
        values.put("year",year);
        values.put("month",month);
        values.put("day",day);
        values.put("steps",step);
        values.put("hot",heat_Str);
        values.put("length", distance_Str);

        //将ContentVvalues 中存储的字段值 存储到step 表中
        long reBack = datasDao.insertValue("step",values);
        //如果存入成功 就将sharedPreferences 中的sport_steps 置0，  监听器中的现有步数置0 发送删除已保存的服务信息 广播到FunctionBroadcastReceiver
        if (reBack > 0){
            SaveKeyValues.putIntValues("sport_steps", 0 );
            StepDetector.CURRENT_SETP = 0;
            Intent bro = new Intent(this, FunctionBroadcastReceiver.class);
            bro.setAction(cancelSaveService);
            sendBroadcast(bro);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("测试服务", "结束了！");
    }

    /**
     * 用判断 应用的 activity是否在运行
     * @param mContext
     * @return
     */
    public static boolean isActivityRunning(Context mContext){
        //通过ActivityManager 获取正在运行的任务信息

        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        //用List 单列集合 参数化类型为ActivityManager.RunningTaskInfo 存储RunningTaskInfo

        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);  //获取1 个任务栈列表,但返回的列表size可能会小于int

        if(info != null && info.size() > 0){
            ComponentName component = info.get(0).topActivity;  //获取当前正在运行的任务栈的顶端activity，通过这个activity可以获取包名、类名等等信息
            if(component.getPackageName().equals(mContext.getPackageName())){
                return true;
            } }
        return false;
    }


    private String formatDouble(Double doubles) {
        DecimalFormat format = new DecimalFormat("####.##");
        String distanceStr = format.format(doubles);
        return distanceStr.equals("0") ? "0.00" : distanceStr;
    }
}

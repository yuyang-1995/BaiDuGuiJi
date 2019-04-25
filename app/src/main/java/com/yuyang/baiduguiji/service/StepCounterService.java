package com.yuyang.baiduguiji.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.yuyang.baiduguiji.receiver.FunctionBroadcastReceiver;
import com.yuyang.baiduguiji.util.StepDetector;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class StepCounterService extends Service {
    public static final String alarmSaveService = "mrkj.healthylife.SETALARM";
    private static final String TAG = "StepCounterService";
    public static Boolean FLAG = false;// 服务运行标志

    private PowerManager mPowerManager;// 电源管理服务
    private PowerManager.WakeLock mWakeLock;// 屏幕灯
    private AlarmManager alarmManager;//闹钟管理器
    private PendingIntent pendingIntent;//延迟意图
    private Calendar calendar;//日期
    private Intent intent;//意图

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //在服务第一次创建服务的时候调用
    //创建服务时就发送定时广播
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "后台服务开始");
        FLAG = true;// 标记为服务正在运行

        // 电源管理服务
        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        //
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "S");
        //保持设备状态
        mWakeLock.acquire();


        //创建定时器
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);//设置时
        calendar.set(Calendar.MINUTE, 59);//分
        calendar.set(Calendar.SECOND, 0);//秒
        calendar.set(Calendar.MILLISECOND, 0);//毫秒

        //通过Intent 启动 BroadcastReceiver 设定intent 的 Action
        intent = new Intent(this, FunctionBroadcastReceiver.class);//发送广播的意图
        intent.setAction(alarmSaveService);//设置Action
        //PendingIntent.getBroadcast包含了sendBroadcast的动作。
        //如果是通过广播来实现闹钟提示的话，PendingIntent对象的获取就应该采用 PendingIntent.getBroadcast(Context c,int i,Intent intent,int j)方法
        pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT); //int FLAG_UPDATE_CURRENT：如果该PendingIntent已经存在，则用新传入的Intent更新当前的数据。

        //启动定时器， 即开启FunctionBroadcastReceiver
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }



    //每次服务启动的时候调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    //服务销毁的时候调用
    @Override
    public void onDestroy() {
        super.onDestroy();
        FLAG = false;// 服务停止
        Log.e(TAG, "后台服务停止");

        if (mWakeLock != null) {
            //释放唤醒资源
            mWakeLock.release();
        }
    }

    public static boolean isActivityRunning(Context mContext) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        if (info != null && info.size() > 0) {
            ComponentName component = info.get(0).topActivity;
            if (component.getPackageName().equals(mContext.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}

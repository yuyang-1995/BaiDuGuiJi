package com.yuyang.baiduguiji.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.activity.PlayActivity;
import com.yuyang.baiduguiji.application.DemoApplication;
import com.yuyang.baiduguiji.service.ExecuteHealthyPlanService;
import com.yuyang.baiduguiji.service.RecordedSaveService;
import com.yuyang.baiduguiji.service.StepCounterService;
import com.yuyang.baiduguiji.util.Constant;
import com.yuyang.baiduguiji.util.SaveKeyValues;

    /**
     * 用于接收定时服务发送的广播
     *  接收后提醒用户
     * Author: yuyang
     * Date:2018/12/15 18:34
     */
    //广播接收器  在接收到服务或 活动发送的广播时 做出相应的处理 一般是
    public class FunctionBroadcastReceiver extends BroadcastReceiver {


    //接收到广播后在onReceive()方法中进行逻辑操作
    //通过
    @Override
    public void onReceive(Context context, Intent intent) {
        //满足开启这个广播的有3个服务

        //获取开启广播接收器服务的 action， 通过action的不同去执行不同的任务
        String action = intent.getAction();
        //
        if (RecordedSaveService.cancelSaveService.equals(action)){

            //关闭记录计步服务
            Intent cancel= new Intent(context,RecordedSaveService.class);
            context.stopService(cancel);
        }

        else if (StepCounterService.alarmSaveService.equals(action)){

            //开启记录计步服务
            Intent start= new Intent(context,RecordedSaveService.class);
            context.startService(start);
        }

        else if (ExecuteHealthyPlanService.planSaveService.equals(action)){
            int taskID;
            int taskNum;
            int taskType;
            //执行运动计划
            int mode = intent.getIntExtra("mode", 1);
            switch (mode){
                case 1://执行的是单个的定时任务
                    taskType = intent.getIntExtra("hint_type",0);
                    Log.e("通知","通知用户进行运动");
                    Log.e("通知","提示类型" + taskType);
                    Log.e("通知","提示计划" + DemoApplication.shuoming[taskType]);
                    sendNotification(context , taskType , DemoApplication.shuoming[taskType]);
                    context.startService(new Intent(context, ExecuteHealthyPlanService.class).putExtra("code", Constant.ONE_PLAN));
                    break;
                case 2://执行的是多个的定时任务
                    Log.e("多个定时任务","此时数据的条数大于1");
                    taskType = intent.getIntExtra("hint_type",0);
                    taskID = intent.getIntExtra("id",0);
                    taskNum = intent.getIntExtra("number",0);
                    //获取当前数据
                    Log.e("通知","通知用户进行运动");
                    Log.e("通知","提示类型" + taskType);
                    Log.e("通知","提示计划" + DemoApplication.shuoming[taskType]);
                    Log.e("数据","数据_ID" + taskID);
                    Log.e("数据","数据序号" + taskNum);
                    sendNotification(context , taskType ,DemoApplication.shuoming[taskType]);
                    //此时任务数量大于1
                    context.startService(new Intent(context, ExecuteHealthyPlanService.class).putExtra("code", Constant.NEXT_PLAN).putExtra("started_num",taskNum).putExtra("started_id",taskID));
                    break;
                case 3://关闭服务
                    Log.e("通知","将要执行关闭服务");
                    int finish_plans = SaveKeyValues.getIntValues("finish_plan", 0);
                    SaveKeyValues.putIntValues("finish_plan", ++finish_plans);
                    context.stopService(new Intent(context, ExecuteHealthyPlanService.class));
                    break;
                default:
                    break;
            }
         }
    }

    /**
     * 发送通知
     * @param context
     * @param type
     * @param messages
     */
    private void sendNotification(Context context ,int type ,String messages){
        SaveKeyValues.putIntValues("do_hint",1);
        SaveKeyValues.putLongValues("show_hint",System.currentTimeMillis());
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context , PlayActivity.class);
        intent.putExtra("play_type", type);
        intent.putExtra("what",1);
        intent.putExtra("do_hint",1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("KeepFit");
        builder.setContentText(messages);
        builder.setSmallIcon(R.mipmap.mrkj_do_sport);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.getNotification());
    }
}

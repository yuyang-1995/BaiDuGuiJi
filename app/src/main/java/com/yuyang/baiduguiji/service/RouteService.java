package com.yuyang.baiduguiji.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.google.gson.Gson;
import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.activity.RunActivity;
import com.yuyang.baiduguiji.activity.RouteDetailActivity;
import com.yuyang.baiduguiji.bean.RoutePoint;
import com.yuyang.baiduguiji.bean.RouteRecord;
import com.yuyang.baiduguiji.database.RouteDBHelper;
import com.yuyang.baiduguiji.map.MyOrientationListener;
import com.yuyang.baiduguiji.util.AllInterface;
import com.yuyang.baiduguiji.util.DateUtils;
import com.yuyang.baiduguiji.util.StepDetector;
import com.yuyang.baiduguiji.util.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

//将运动过程中的计算逻辑都放在Service中
//        当前位置:我的异常网» Android » Android使用百度LBS SDK（4）记录和显示行走轨迹
//        Android使用百度LBS SDK（4）记录和显示行走轨迹
//        www.MyException.Cn 网友分享于：2015-04-01浏览：0次
//        Android使用百度LBS SDK（四）记录和显示行走轨迹
//        记录轨迹思路
//        用Service获取经纬度，onCreate中开始采集经纬度点，保存到ArrayList
//        每隔5秒取样一次，若经纬度未发生变化，丢弃该次取样
//        在onDestroy中，将ArrayList转成JSON格式，然后存储到SDCard中
//        显示轨迹思路
//        读取目录下所有轨迹文件，并生成ListView
//        在OnItemClick中将文件名称通过intent.putExtra传递给显示轨迹的Activity
//        根据文件名将对应的JSON内容转成ArrayList
//        然后将以上ArrayList的点集依次连线，并绘制到百度地图上
//        设置起始点Marker，Zoom级别,中心点为起始点
//        轨迹点小于2个无法绘制轨迹，给出提示
//        LBS
//
  public class RouteService extends Service {

    private double currentLatitude, currentLongitude;

    private LocationClient mlocationClient = null;
    private MylocationListener mlistener;
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    //private String rt_time, rt_distance,rt_step;
    //定位图层显示方式
    private MyLocationConfiguration.LocationMode locationMode;

    //获取运动期间的步数
    private SensorManager mSensorManager;// 传感器服务
    public StepDetector detector;// 传感器监听对象


    //声明AllInterface类中的IUpdateLocation接口
    AllInterface.IUpdateLocation iUpdateLocation;
    public  ArrayList<RoutePoint> routPointList = new ArrayList<RoutePoint>();  //路程信息集合， 包括坐标  时间  跑步速度
    public  int totalDistance = 0;
    private int totalStep = 0;
    public  long beginTime = 0, totalTime = 0;
    private String showDistance,showTime, showStep;   //这是哪个参数用于历史运动记录查询
    Notification notification; //通知
    RemoteViews contentView;  //通知栏

    public void setRunning(boolean running) {
        isRunning = running;
    }

    //声明是否在运动的布尔值
    public boolean isRunning=true;

    //private Gson gson;


    public void setiUpdateLocation(AllInterface.IUpdateLocation iUpdateLocation) {
        this.iUpdateLocation = iUpdateLocation;
    }

       @Override
    //第一次创建服务
    public void onCreate() {
        super.onCreate();
        //开始时间

           // 创建监听器类，实例化监听对象
           detector = new StepDetector(this);//实例化传感器对象
           detector.walk = 1;//设置步数从一开始
           // 获取传感器的服务，初始化传感器
           mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

           //注册传感器事件监听事件
           mSensorManager.registerListener(detector, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

           Bmob.initialize(this,"ab0ec75caa7276feb0d7139d694bb3e0");
        beginTime = System.currentTimeMillis();
        isRunning=true;

//        RouteDBHelper dbHelper = new RouteDBHelper(this);
//        // 只有调用了DatabaseHelper的getWritableDatabase()方法或者getReadableDatabase()方法之后，才会创建或打开一个连接
//        SQLiteDatabase sqliteDatabase = dbHelper.getReadableDatabase();
        totalTime = 0;
        totalDistance = 0;
        totalStep = 0;
        //每开启一次运动， RoutePoint集合都需要清除
        routPointList.clear();
   }
        @Override
      //启动服务
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("yuyang", "RouteService--------onStartCommand---------------");
        //启动服务后初始化定位
        //初始化Notification
        initLocation();//初始化LocationgClient
        initNotification(); //初始化Notification
        //保持界面常亮
        Utils.acquireWakeLock(this);
        showStep = "";

            // 开启轨迹记录线程
        return super.onStartCommand(intent, flags, startId);
    }

    //首先来介绍一下系统自带的通知（Notification）的使用。Notification的使用有两种方法，
    // 分别是Notification直接创建的方式和使用Notification.Builder创建者模式创建的方式。
       //初始化Notification
    private void initNotification() {
        int icon = R.mipmap.hwpb;
        contentView = new RemoteViews(getPackageName(), R.layout.notification_layout);  //顶部通知栏
        notification = new NotificationCompat.Builder(this).setContent(contentView).setSmallIcon(icon).build(); //将通知栏包装在通知中

        Intent notificationIntent = new Intent(this, RunActivity.class); //将Intent发送到跑步活动中
        notificationIntent.putExtra("flag", "notification");
        notification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    }

       //初始化LocationgClient
    private void initLocation() {
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.location_marker);

        locationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mlocationClient = new LocationClient(this);
        mlistener = new MylocationListener();
//        initMarkerClickEvent();
        //注册监听器
        mlocationClient.registerLocationListener(mlistener);
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption mOption = new LocationClientOption();
        //设置坐标类型
        mOption.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        mOption.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        mOption.setScanSpan(1000);
        //设置 LocationClientOptionƒ20
        mlocationClient.setLocOption(mOption);

        //初始化图标,BitmapDescriptorFactory是bitmap 描述信息工厂类.
        mIconLocation = BitmapDescriptorFactory
                .fromResource(R.mipmap.location_marker);

        myOrientationListener = new MyOrientationListener(this);

        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
            }
        });


//        mSearch = RoutePlanSearch.newInstance();
//        mSearch.setOnGetRoutePlanResultListener(this);
//        //开启定位
//        mBaiduMap.setMyLocationEnabled(true);
        if (!mlocationClient.isStarted()) {
            mlocationClient.start();
        }
        myOrientationListener.start();
    }



    //给顶部数据栏显示实时运动数据
    private void startNotifi(String time, String distance, String step) {
        startForeground(1, notification);
        contentView.setTextViewText(R.id.bike_time, time);
        contentView.setTextViewText(R.id.bike_distance, distance);
        contentView.setTextViewText(R.id.bike_step, step);
      }


    public IBinder onBind(Intent intent) {
        Log.d("yuyang", "onBind-------------");
        return null;
    }

    public boolean onUnBind(Intent intent) {
        Log.d("yuyang", "onBind-------------");
        return false;
    }

      //结束服务的时候， 关闭mlocationClient、myOrientationListener等资源, 将数据封装到Bundle中，传递到RouteDetailActivity， 并将数据添加到表中
    @Override
    public void onDestroy() {
        super.onDestroy();
        mlocationClient.stop();
        myOrientationListener.stop();
        Log.d("yuyang", "RouteService----0nDestroy---------------");
        Gson gson = new Gson();
        String  routeListStr = gson.toJson(routPointList);  //将坐标点集合转化为json字符串
        //建立Bundle 将运动结束的数据传给RouteDetailActivity
        Bundle bundle = new Bundle();
        bundle.putString("totalTime", showTime );  //运动总时间
        bundle.putString("totalDistance", showDistance ); //运动总距离
        bundle.putString("totalStep", showStep);   //运动总步数
        bundle.putString("routePoints", routeListStr);   //运动中保存的路径坐标
        //服务结束的时候， 跳转到RouteDetailActivity， 并将此次运动 数据运动时间， 运动距离， 步数 RoutePoint集合传递到RouteDetailActivity
        Intent intent = new Intent(this, RouteDetailActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //新的任务栈
        startActivity(intent);

        Map<String,Object> mapDate = DateUtils.getDate();
        String showDate = mapDate.toString();
        RouteRecord record = new RouteRecord();
        // public String cycle_date;  //日期
        //    public String cycle_time;  //运动时长
        //    public String cycle_distance; //距离
        //    public String cycle_points;  //坐标点
        //    public String cycle_step;      //运动步数
        record.setCycle_date(showDate);
        record.setCycle_distance(showDistance);
        record.setCycle_time(showTime);
        record.setCycle_step(showStep);


        record.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e!=null){
                    Toast.makeText(getBaseContext(), "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getBaseContext(), "运动数据云存储成功",Toast.LENGTH_SHORT).show();
                }
            }
        });




        //如果总RoutePoint 集合大于2， 将运动数据添加到运动记录表中
        if (routPointList.size() > 2)
            insertData(routeListStr);

        Utils.releaseWakeLock();  //释放电源锁

        if (detector != null) {
            //取消对所有传感器的监听
            mSensorManager.unregisterListener(detector);
        }

        stopForeground(true);  //关闭顶部通知栏
        showTime="";
        showDistance="";
        showStep="";
        isRunning=false;
    }


    //服务开启中监听位置变化， 只要位置在变化， 运动路程， 坐标，时间就在变化
    public class MylocationListener implements BDLocationListener {
        //定位请求回调接口
        private boolean isFirstIn = true;

        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (null == bdLocation) return; //返回方法

            if (!isRunning) return;  //返回方法


            //"4.9E-324"表示目前所处的环境（室内或者是网络状况不佳）造成无法获取到经纬度
            if ("4.9E-324".equals(String.valueOf(bdLocation.getLatitude())) || "4.9E-324".equals(String.valueOf(bdLocation.getLongitude()))) {
                return;
            }//过滤百度定位失败

            //当满足监听器对象不为空， 处于跑步状态和gps信号良好三个条件时， 才开始获取并存储坐标点集合

            double routeLat = bdLocation.getLatitude();
            double routeLng = bdLocation.getLongitude();

            //在运动过程中， 将运动的实时坐标、时间和速度 封装到一个RoutePoint类中
            RoutePoint routePoint = new RoutePoint(); //获取经纬度之后将其设置进入RoutePoint
            routePoint.setRouteLat(routeLat);
            routePoint.setRouteLng(routeLng);

            if (routPointList.size() == 0)  //当坐标点集合为空
                routPointList.add(routePoint);
                 else {                     //当坐标点集合不为空则将
                 RoutePoint lastPoint = routPointList.get(routPointList.size() - 1);//最后一个坐标点

                if (routeLat == lastPoint.getRouteLat() && routeLng == lastPoint.getRouteLng()) { //当用户坐标未改变时

                } else { //当用户坐标改变时， 获取并存储新的坐标

                    LatLng lastLatLng = new LatLng(lastPoint.getRouteLat(),lastPoint.getRouteLng()); //最后坐标的经纬度对象
                    LatLng currentLatLng = new LatLng(routeLat, routeLng);   //当前坐标的经纬度对象

                    if (routeLat > 0 && routeLng > 0) {  //当前坐标经纬度有效时
                        double distantce = DistanceUtil.getDistance(lastLatLng, currentLatLng);  //用百度API获取集合中最后坐标点和当前坐标点之间的距离
                           // 大于2米算作有效加入列表
                        if (distantce > 2) {
                            //distance单位是米 转化为km/h
                            routePoint.speed = Double.parseDouble(String.format("%.1f", (distantce/1000)*30*60));  //
                            routePoint.time=System.currentTimeMillis();  //RoutePoint时间为结束时间
                            routPointList.add(routePoint);   //将每一个RoutePoint 类封装到集合中。
                            totalDistance += distantce;  //运动总距离
                        }
                    }
                }
            }

            totalTime = (int) (System.currentTimeMillis() - beginTime) / 1000 / 60; //运动所用总时间

            showDistance=totalDistance + "米";

            if(totalTime>60) {
                showTime=totalTime/60+"时"+totalTime%60+"分";
            }

            else showTime=totalTime + "分钟";

            totalStep = StepDetector.CURRENT_SETP;   //获取记不起所记录的步数
            //showPrice=totalPrice+ "步";
            showStep = totalStep + "步";

            //将在运动期间的运动路程， 运动路程， 运动步数后， 在顶部通知栏进行展示
            showRouteInfo(showTime,showDistance,showStep);
 }
    }//



       // 进行实时路程展示， 分别在跑步活动和顶部通知栏展示
       //只要服务开启， 并且手机坐标在改变就发送广播到RunActivity， 使RunActivity中控件中的数据得到及时的刷新
       private void showRouteInfo(String time,String distance, String step ){
        Intent intent = new Intent("com.locationreceiver");  //将在服务中获取的个数值设置Intent的action， 传递到RunActivity中
        Bundle bundle = new Bundle();
        bundle.putString("totalTime", time);     //运动过程用时
        bundle.putString("totalDistance", distance);   //运动过程路径
        bundle.putString("totalStep", step);       //运动过程的步数
        intent.putExtras(bundle);
        sendBroadcast(intent);   //通过发送广播将数据发送给RunActivity

        startNotifi(time, distance, step); //在运动中给顶部通知栏显示运动时的数据
    }

    //
    public static class NetWorkReceiver extends BroadcastReceiver{
        public NetWorkReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo.State wifiState = null;
            NetworkInfo.State mobileState = null;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED == mobileState) {
//                Toast.makeText(context, context.getString(R.string.net_mobile), Toast.LENGTH_SHORT).show();
                // 手机网络连接成功
            } else if (wifiState != null && mobileState != null
                    && NetworkInfo.State.CONNECTED != wifiState
                    && NetworkInfo.State.CONNECTED != mobileState) {
//                Toast.makeText(context, context.getString(R.string.net_none), Toast.LENGTH_SHORT).show();

                // 手机没有任何的网络
            } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {
                // 无线网络连接成功
//                Toast.makeText(context, context.getString(R.string.net_wifi), Toast.LENGTH_SHORT).show();

            }
        }
    }



    //将每次运动后将数据项大于2的数据集合插入到运动记录表中
    public void insertData(String routeListStr) {
        //ContentValues 和HashTable类似都是一种存储的机制 但是两者最大的区别就在于，
        // contenvalues只能存储基本类型的数据，像string，int之类的，不能存储对象这种东西，而HashTable却可以存储对象。
        // ContentValues存储对象的时候，以(key,value)的形式来存储数据。
        ContentValues values = new ContentValues();
        // 向该对象中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据当中的数据类型一致
        //SQLite数据库中保存5个字段数据
        //记录每一次运动的数据
        values.put("cycle_date", Utils.getDateFromMillisecond(beginTime));  //记录运动开始时间
        values.put("cycle_time", showTime);  //运动所用时间
        values.put("cycle_distance", showDistance);  //运动距离
        values.put("cycle_step", showStep);  //运动步数
        values.put("cycle_points", routeListStr);  //运动所有坐标点
        // 创建DatabaseHelper对象
        RouteDBHelper dbHelper = new RouteDBHelper(this);
        // 得到一个可写的SQLiteDatabase对象
        SQLiteDatabase sqliteDatabase = dbHelper.getWritableDatabase();
        // 调用insert方法，就可以将数据插入到数据库当中
        // 第一个参数:表名称
        // 第二个参数：SQl不允许一个空列，如果ContentValues是空的，那么这一列被明确的指明为NULL值
        // 第三个参数：ContentValues对象
        sqliteDatabase.insert("cycle_route", null, values);
        sqliteDatabase.close();
    }

}

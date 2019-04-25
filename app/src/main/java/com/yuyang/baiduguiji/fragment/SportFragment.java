package com.yuyang.baiduguiji.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.activity.PlayActivity;
import com.yuyang.baiduguiji.activity.RunActivity;
import com.yuyang.baiduguiji.base.BaseFragment;
import com.yuyang.baiduguiji.bean.RouteRecord;
import com.yuyang.baiduguiji.database.RouteDBHelper;
import com.yuyang.baiduguiji.gson.HeWeather;
import com.yuyang.baiduguiji.json.Weatherjson;
import com.yuyang.baiduguiji.service.RouteService;
import com.yuyang.baiduguiji.service.StepCounterService;
import com.yuyang.baiduguiji.util.Constant;
import com.yuyang.baiduguiji.util.MyCallBack;
import com.yuyang.baiduguiji.util.MyHttp;
import com.yuyang.baiduguiji.util.SaveKeyValues;
import com.yuyang.baiduguiji.util.StepDetector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import mrkj.library.wheelview.circlebar.CircleBar;


public class SportFragment extends BaseFragment {
    //private static final int WEATHER_MESSAGE = 1;
    private static final int STEP_PROGRESS = 2;
    private View view;
    private TextView city_name, city_temperature,city_air_quality,show_heat;

    //显示精度的圆形进度条
    private CircleBar circleBar;
    private TextView show_mileage, want_steps,warm_btn,no_data;
        //跳转按钮

    //下载天气预报的相关信息
    /*private TodayInfo todayInfo;    //今日的天气
    private PMInfo pmInfo;*/     //今日空气质量
    private String weather_url;  //天气接口
    private String query_city_name; //城市名称*/

    //展示进度， 里程， 热量的相关参数
    private int custom_steps; //用户目标步数
    private int custom_step_length;   //步长
    private int custom_weight;  //体重
  //  private Thread get_step_thread;  //定义线程对象
  //  private Intent step_service;    //计步服务
    //private boolean isStop;       //是否运行子线程
    private Double distance_values;   //路程：米
    private int step_values;   //步数
    private Double heat_values;   //热量
    private int duration;      //动画时间
    private Context context;
    private SQLiteDatabase db;

    private String close_step;  //数据库中最近的一次步数
    RouteRecord routeRecord;      //
    String TABLE_NAME = "cycle_route";  //表名
    private RouteDBHelper dbHelper;

    //定位城市的天气Id和城市名
    public static String locationCountyWeatherId = null;
    public static String locationCountyWeatherName = null;

    //定位返回指针
    //BaiduLocation baiduLocation;



    //获取和风天气的key---自己的
    public static final String KEY = "a0187789a4424bc89254728acd4a08ed";

    private LocationClient mlocationClient = null;
    private MyWeatherlocationListener mlistener;  //实例化监听接口

     HeWeather heWeather;



    private String cityname, tempture, weather;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    showWeatherInfo(heWeather);
                    break;
              }
        }
    };



    //在碎片与关联活动建立联系的时候回调
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;   //获取上下文
    }

    /**
     * 把下载的数值解析后赋值给相关的控件
     * @param resultStr
     */
   private void setDownLoadMessageToView(String resultStr) {
        //通过下载类的解析方法， 将json数据解析为TodayInfo 和 PMInfo
        /*todayInfo = HttpUtils.parseNowJson(resultStr);  //获取当天天气数据
        pmInfo = HttpUtils.parsePMInfoJson(resultStr);  //获取PM2.5的数据
        //如果碎片加入到活动 寄给碎片中的控件设值
        if (isAdded()) {
            city_name.setText(context.getString(R.string.city) + query_city_name);
            city_temperature.setText(context.getString(R.string.temperature_hint));
            //city_air_quality.setText(context.getString(R.string.quality) + pmInfo.getQuality());
        }*/
    }


    /**
     * 创建于碎片相关的视图
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //
        view = inflater.inflate(R.layout.fragment_sport, null);
        initView();  //初始化视图
        initValues();  //初始化数据
        setNature();  //设置功能
        //提示
        if (StepDetector.CURRENT_SETP > custom_steps) {
            Toast.makeText(getContext(), "真厉害， 此次跑步目标已经完成了!", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    /**
     * 计算并格式化doubles数值， 保留两位有效数字
     * @param ints
     * @return
     */
    private String formatDouble(int ints) {
        DecimalFormat format = new DecimalFormat("####.##");
        String distanceStr = format.format(ints);
        return distanceStr.equals("0") ? "0.00" : distanceStr;  //格式化返回String 类型
    }

    //设置相关属性
    private void setNature() {
        //设置初始的进度
        circleBar.setcolor(R.color.theme_blue_two);   //设置进度条颜色
        circleBar.setMaxstepnumber(custom_steps);       //设置进度条最大值
      //  getServiceValue();   //获取计步服务的信息

        //跳转跑步界面按钮
        warm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(context, "跳转热身界面", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), RunActivity.class));
                 }
        });
             want_steps.setText("跑步目标: " + custom_steps + "步");
    }
       //初始化值
    private void initValues() {
        //1、获取所在城市并获取该城市的天气信息
        query_city_name = SaveKeyValues.getStringValues("city", "北京");
        //下面是获取天气信息数据

        dbHelper = new RouteDBHelper(this.getContext());

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mlocationClient = new LocationClient(this.getContext());
        mlocationClient.start();
        mlistener = new MyWeatherlocationListener();
//        initMarkerClickEvent();
        //注册监听器
        mlocationClient.registerLocationListener(mlistener);


        //2、获取计算里程和热量的相关参数-->默认步数：1000、步长：70cm、体重：50kg
       // isStop = false;    //子线程是否开启
        duration = 800;   //动画时间
        //获取默认值用于计算公里数和消耗的热量
        custom_steps = SaveKeyValues.getIntValues("step_plan",6000);//用户计划的步数
        custom_step_length = SaveKeyValues.getIntValues("length",70);//用户的步长
        custom_weight = SaveKeyValues.getIntValues("weight", 50);//用户的体重

         db = dbHelper.getWritableDatabase();
         int id = (int) getItemCount();

        if (id>0){
             no_data.setVisibility(View.GONE);
             //查询表中所有数据

             //String sql = "select * from" + TABLE_NAME + "where route_id order by route_id desc limit 0,1";
             Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
             //判断curse不为空
             if (cursor!=null){
                 cursor.moveToLast();
                 RouteRecord point = new RouteRecord();
                 point.setCycle_step(cursor.getString(cursor.getColumnIndex("cycle_step"))); //运动步数
                 String step = point.getCycle_step();
                 Log.e("步数", step);
                 String[] strings = step.split("步");
                 String st = strings[0];
                 int stnum = Integer.parseInt(st); //步数
                 String distances = cursor.getString(cursor.getColumnIndex("cycle_distance")); //运动距离
                // Double distance = Double.valueOf(Integer.parseInt(distances));    //运动里程
                 String[] strings1 = distances.split("米");

                 Double mnum = Double.parseDouble(strings1[0])/1000;
                 String m = formatDouble(mnum);
                 show_mileage.setText("跑步" + m + "公里"); //显示里程数据
                 circleBar.update(stnum, duration);
                //消耗热量： 跑步热量(kcal) = 体重(kg) * 距离(km) * 1.036;
                 heat_values = custom_weight * mnum * 1.036;
                 String heat = formatDouble(heat_values);
                 show_heat.setText("消耗"+ heat + "大卡");  //显示热量*/
             }
             cursor.close();
        }else{
             no_data.setVisibility(View.VISIBLE);
             show_mileage.setVisibility(View.GONE);
             show_heat.setVisibility(View.GONE);
         }
    }

    private String formatDouble(Double doubles) {
        DecimalFormat format = new DecimalFormat("####.##");
        String distanceStr = format.format(doubles);
        return distanceStr.equals("0") ? "0.00" : distanceStr;  //格式化返回String 类型
    }

    //从cycle_route表中 获取记录条数
   public long getItemCount() {
        String sql = "select count(*) from " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        long count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        circleBar = (CircleBar) view.findViewById(R.id.show_progress);   //显示进度的CircleBar
        city_name = (TextView) view.findViewById(R.id.city_name);
        city_temperature = (TextView) view.findViewById(R.id.temperature);
        city_air_quality = (TextView) view.findViewById(R.id.air_quality);
        warm_btn = (TextView) view.findViewById(R.id.warm_up);
        show_mileage = (TextView) view.findViewById(R.id.mileage_txt);
        show_heat = (TextView) view.findViewById(R.id.heat_txt);
        want_steps = (TextView) view.findViewById(R.id.want_steps);
        no_data = (TextView) view.findViewById(R.id.no_data);

    }

    private class MyWeatherlocationListener implements BDLocationListener {

           @Override
        public void onReceiveLocation(BDLocation bdLocation) {
               if (null != bdLocation && bdLocation.getLatitude() != BDLocation.TypeServerError){
                   String currentLat = String.valueOf(bdLocation.getLatitude()); //纬度
                   String currentLog = String.valueOf(bdLocation.getLongitude()); //经度
                   Log.e("经纬度：", currentLat+ currentLog);

                   locationCountyWeatherId = currentLog.substring(0,currentLog.indexOf('.') + 4) + ","
                           + currentLat.substring(0, currentLat.indexOf('.') + 4);
                   String weatherUrl = "https://free-api.heweather.com/v5/weather?city="
                           + locationCountyWeatherId + "&key=" + KEY;
                    requestWeatherAsync(weatherUrl);  //
                }}
       }

         //根据url访问服务器获取天气信息
    private void requestWeatherAsync(String weatherUrl) {

        MyHttp.sendRequestOkHttpForGet(weatherUrl, new MyCallBack() {
            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "获取天气失败，请检查手机网络", Toast.LENGTH_SHORT).show();
                Log.e("heWeather isOk?", "fail");
         }

            @Override
            public void onResponse(String response) throws IOException {
                final  String responseText = response;
                  heWeather = Weatherjson.getWeatherResponse(responseText);
                     if (heWeather != null && "ok".equals(heWeather.status)) {
                            //显示天气数据
                         Log.e("heWeather isOk?", heWeather.status);
                            Log.d("timeTest", "WeatherActivity show start");

                            handler.sendEmptyMessage(1);

                        }
               }
        });
       }

       //展示
    private void showWeatherInfo(HeWeather heWeather) {
        cityname = heWeather.basic.cityName;
        tempture = heWeather.now.tmp;
        weather = heWeather.now.cond.txt;
        Log.e("cityname",cityname);
        Log.e("tempture",tempture);
        Log.e("weather",weather);
        city_name.setText(cityname);
        city_temperature.setText(tempture + "°");
        city_air_quality.setText("天气:" + weather);
       }


    //判断是否有网络
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    //    handler.removeCallbacks(get_step_thread);  //移除监听
    //    isStop = true;          //设置线程开关可以关闭
    //    get_step_thread = null;    //清空线程对象
        step_values = 0;          //设置初始步数为0
        duration = 800;             //设置初始值动画时间为800ms

     }
}

package com.yuyang.baiduguiji.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.base.BaseActivity;
import com.yuyang.baiduguiji.bean.RoutePoint;
import com.yuyang.baiduguiji.map.MyOrientationListener;
import com.yuyang.baiduguiji.service.RouteService;
import com.yuyang.baiduguiji.service.StepCounterService;
import com.yuyang.baiduguiji.util.AllInterface;
import com.yuyang.baiduguiji.util.OverlayManager;
import com.yuyang.baiduguiji.util.StepDetector;
import com.yuyang.baiduguiji.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunActivity extends BaseActivity implements View.OnClickListener, AllInterface.OnMenuSlideListener {

    private double currentLatitude, currentLongitude, changeLatitude, changeLongitude;
    private ImageView  btn_locale,  back_icon;
   // public static TextView current_addr;
    private TextView end_route, title, tv_sos;
    private LinearLayout bike_layout;
    private TextView  prompt,prompt1,textview_time, textview_distance, unlock, tv_step_count;// textview_price,
    public static TextView run_distance, run_time,run_step;
    private long exitTime = 0;
    private boolean isFirstIn;
    //自定义图标
    private BitmapDescriptor mIconLocation, dragLocationIcon, bikeIcon, nearestIcon;

   // RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    //定位图层显示方式
    private MyLocationConfiguration.LocationMode locationMode;
    //  private BikeInfo bInfo;

     //PlanNode 规划路线
    PlanNode startNodeStr, endNodeStr;
    int nodeIndex = -1;
    WalkingRouteResult nowResultwalk = null;
    boolean useDefaultIcon = true, hasPlanRoute = false, isServiceLive = false;
    RouteLine routeLine = null;
    //
    //提供一些基于基础覆盖而组合而成的高级覆盖物，包括用于显示poi数据，规划路线，公交详情路线的覆盖物
    //覆盖图层管理器
    OverlayManager routeOverlay = null;

    LatLng currentLL;

    View shadowView;
    // 定位相关
    LocationClient mlocationClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private MyOrientationListener myOrientationListener;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private float mCurrentX;
    private boolean isFirstLoc = true; // 是否首次定位
    private final int DISMISS_SPLASH = 0;
    private static String time, distance, step;


    //声明是否在运动的布尔值
    private boolean isRunning=false;

    private ArrayList<RoutePoint> routePoints = new ArrayList<RoutePoint>(); //坐标点集合
    private  ArrayList<LatLng> needDPoints = new ArrayList<LatLng>(); //需要画出的坐标点集合， 存储最后一个坐标和最新的坐标点
    private RunlocationListener routeLocationListener;

   Handler handler = new Handler(){

       @Override
       public void handleMessage(Message msg) {
           switch (msg.what){
               case 1:
                   if (needDPoints!=null && needDPoints.size()>=2){
                       //
                       OverlayOptions ooPolyline = new PolylineOptions().width(8).color(0xFF36D19D).points(needDPoints);

                       //在地图上显示
                       mBaiduMap.addOverlay(ooPolyline);
                    }
             }
           super.handleMessage(msg);
       }
   };



    public RunActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //设置此界面为竖屏

        SDKInitializer.initialize(getApplicationContext());//在Application的onCreate()不行，必须在activity的onCreate()中
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_run);
        Log.d("yuyang", "RunActivity---------onCreate---------------");
        setStatusBar();
        initMap(); //初始化地图
        initView();  //初始化View
        isServiceLive = Utils.isServiceWork(this, "com.yuyang.baiduguiji.service.RouteService"); //判断RouteService是否处于工作状态
        if (isServiceLive)
            beginService();
       }

    @Override
    protected void setActivityTitle() {

    }

    @Override
    protected void getLayoutToView() {

    }

    @Override
    protected void initValues() {

    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void setViewsListener() {

    }

    @Override
    protected void setViewsFunction() {

    }



    //地图初始化
    private void initMap() {
        mMapView = (MapView) findViewById(R.id.id_bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mlocationClient = new LocationClient(this);
        mlocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();

        routeLocationListener = new RunlocationListener();
        //注册监听器
        mlocationClient.registerLocationListener(routeLocationListener);

        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(2000);//设置onReceiveLocation()获取位置的频率
        option.setIsNeedAddress(true);//如想获得具体位置就需要设置为true
        mlocationClient.setLocOption(option);
        mlocationClient.start(); //开始定位
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;  //定位模式为跟随
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        myOrientationListener = new MyOrientationListener(this);
        //通过接口回调来实现实时方向的改变
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
        myOrientationListener.start(); //定位箭头方向箭头

    }

    /**
     * 定位SDK监听函数
     */
    //位置
    public class MyLocationListenner implements BDLocationListener {

        //位置发生改变回调的方法
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // map view 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()  //用户位置信息
                    .accuracy(bdLocation.getRadius())
                    .direction(mCurrentX)//设定图标方向     // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();

            mBaiduMap.setMyLocationData(locData);  //将位置信息注入到地图
            currentLatitude = bdLocation.getLatitude();  //当前坐标
            currentLongitude = bdLocation.getLongitude();
          //  current_addr.setText(bdLocation.getAddrStr());
            currentLL = new LatLng(bdLocation.getLatitude(),
                    bdLocation.getLongitude());//当前坐标
            com.yuyang.baiduguiji.util.LocationManager.getInstance().setCurrentLL(currentLL);//设置当前坐标
            com.yuyang.baiduguiji.util.LocationManager.getInstance().setAddress(bdLocation.getAddrStr()); //设置当前位置


            //首次定位
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                //首次定位需要设置缩放比
                //地图缩放比设置为18
                builder.target(ll).zoom(18.0f);  //
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//将新的map状态注入到Map的animateMapStatus方法中
                changeLatitude = bdLocation.getLatitude(); //第一次获取位置
                changeLongitude = bdLocation.getLongitude();
                //如果停止服务了， 将现在位置加入到视图
                if (!isServiceLive) {
                    addOverLayout(currentLatitude, currentLongitude);
                } }} }

      //初始化界面
    private void initView() {
        bike_layout = (LinearLayout) findViewById(R.id.bike_layout);
        bike_layout.setVisibility(View.GONE);


       //三个实时文本框
        run_time = (TextView) findViewById(R.id.bike_time);  //跑步时间
        run_distance = (TextView) findViewById(R.id.bike_distance);  //跑步路程
        run_step = (TextView) findViewById(R.id.tv_stpe_num);  //步数


        //三个固定的文本框， 在运动中无需改变
        textview_time = (TextView) findViewById(R.id.textview_time);
        textview_distance = (TextView) findViewById(R.id.textview_distance);
        tv_step_count = (TextView) findViewById(R.id.tv_step_count);

        unlock = (TextView) findViewById(R.id.unlock);//开启跑步控件

        prompt = (TextView) findViewById(R.id.prompt);
        prompt1 = (TextView) findViewById(R.id.prompt1);
        /*cancel_book = (TextView) findViewById(R.id.cancel_book);
        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);*/
        shadowView = (View) findViewById(R.id.shadow);
        back_icon = (ImageView) findViewById(R.id.back_icon);
        // bike_sound.setOnClickListener(this);
        back_icon.setOnClickListener(this);
        shadowView.setOnClickListener(this);
//        mLeftDrawerLayout.setListener(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(this, 50));
        layoutParams.setMargins(0, statusBarHeight, 0, 0);//4个参数按顺序分别是左上右下
//        title_layout.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d("yuyang", "statusBarHeight---------------" + statusBarHeight);
        layoutParams2.setMargins(40, statusBarHeight + Utils.dp2px(RunActivity.this, 50), 0, 0);//4个参数按顺序分别是左上右下

         mBaiduMap = mMapView.getMap();  //获取Map

        mBaiduMap.setOnMapStatusChangeListener(changeListener);//给地图注入状态监听器
        btn_locale = (ImageView) findViewById(R.id.btn_locale);  //定为按钮

       // btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
        end_route = (TextView) findViewById(R.id.end_route);  //结束跑步按钮
        end_route.setVisibility(View.GONE);
        tv_sos = findViewById(R.id.sos);
        tv_sos.setVisibility(View.GONE);

        title = (TextView) findViewById(R.id.title);
        //给各个按钮设置监听器
        btn_locale.setOnClickListener(this); //定位按钮
        end_route.setOnClickListener(this); //结束运动按钮
        tv_sos.setOnClickListener(this);  //紧急救助按钮
        mMapView.setOnClickListener(this);  //Map
        //地理图标和自行车图标
        dragLocationIcon = BitmapDescriptorFactory.fromResource(R.mipmap.drag_location);
    }


     //获取自己的定位
    public void getMyLocation() {
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);   //注入坐标更新地图状态
        mBaiduMap.setMapStatus(msu);  //设置新状态
    }

     //各个点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
           //定位按钮
            case R.id.btn_locale:
                //将定位放置地图中心
                getMyLocation();
                if (routeOverlay != null)
                    //移除覆盖图层
                    routeOverlay.removeFromMap();

                //将当前位置放置在覆盖图层中
                addOverLayout(currentLatitude, currentLongitude);
                break;

            case R.id.back_icon:
               if (end_route.getVisibility() == View.VISIBLE){
                    toastDialog1();
                }else {
                    Intent intent = new Intent(RunActivity.this, FunctionActivity.class);
                    startActivity(intent);
                    finish();
                }
                  break;
              //结束跑步
            case R.id.end_route:
                toastDialog();
                 break;

                 //紧急求助
            case R.id.sos:
                toastDialog2();
               break;
                default:
                   break;
         }
    }



    @Override
    public void onMenuSlide(float offset) {
        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
        int alpha = (int) Math.round(offset * 255 * 0.4);
        String hex = Integer.toHexString(alpha).toUpperCase();
        Log.d("yuyang", "color------------" + "#" + hex + "000000");
        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }


     //地图状态监听器
     private BaiduMap.OnMapStatusChangeListener changeListener = new BaiduMap.OnMapStatusChangeListener() {

         public void onMapStatusChangeStart(MapStatus mapStatus) {
         }

         @Override
         public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

         }

         //地图状态改变结束
         @Override
         public void onMapStatusChangeFinish(MapStatus mapStatus) {
             String _str = mapStatus.toString();
             String _regex = "target lat: (.*)\ntarget lng";
             String _regex2 = "target lng: (.*)\ntarget screen x";
             changeLatitude = Double.parseDouble(latlng(_regex, _str));
             changeLongitude = Double.parseDouble(latlng(_regex2, _str));
             LatLng changeLL = new LatLng(changeLatitude, changeLongitude);
             startNodeStr = PlanNode.withLocation(changeLL);
          }

         public void onMapStatusChange(MapStatus mapStatus) {
         }
     };


    private String latlng(String regexStr, String str) {
        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            str = matcher.group(1);
        }
        return str;
    }

   //将坐标添加到图层
    private void addOverLayout(double _latitude, double _longitude) {
        //先清除图层
        mBaiduMap.clear();
        mlocationClient.requestLocation();

        // 定义Maker坐标点
        LatLng point = new LatLng(_latitude, _longitude); //Marker坐标即为定位坐标中心
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(point)
                .icon(dragLocationIcon);
        // 在地图上添加Marker，并显示
        mBaiduMap.addOverlay(options);

    }

     //点击开始跑步，开启服务
    public void gotoCodeUnlock(View view) {
            //开启服务
            beginService();
    }

    public void gotoMyRoute(View view) {
        startActivity(new Intent(this, MyRouteActivity.class));
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("yuyang", "RunActivity------------onStart------------------");
    }

     //回到当前Activity
    protected void onRestart() {
        super.onRestart();
        mBaiduMap.setMyLocationEnabled(true);
        mlocationClient.start();
        myOrientationListener.start();
        mlocationClient.requestLocation();
        isServiceLive = Utils.isServiceWork(this, "com.yuyang.baiduguiji.service.RouteService");
        Log.d("yuyang", "RunActivity------------onRestart------------------");

        if (RouteDetailActivity.completeRoute)    //当
            backFromRouteDetail();
    }

      //从流程细节跳转回来
    private void backFromRouteDetail() {
        isFirstIn = true;
        title.setText(getString(R.string.bybike));
        textview_time.setText(getString(R.string.foot));
        textview_distance.setText(getString(R.string.distance));

        bike_layout.setVisibility(View.GONE);
        prompt.setVisibility(View.GONE);
        prompt1.setVisibility(View.VISIBLE);
        //current_addr.setVisibility(View.VISIBLE);
        back_icon.setVisibility(View.VISIBLE);
       // book_bt.setVisibility(View.VISIBLE);
        unlock.setVisibility(View.VISIBLE);
   //     divider.setVisibility(View.VISIBLE);
  //      btn_refresh.setVisibility(View.VISIBLE);
        btn_locale.setVisibility(View.VISIBLE);
        end_route.setVisibility(View.GONE);
        tv_sos.setVisibility(View.GONE);
        mMapView.showZoomControls(true);

        getMyLocation();
        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        addOverLayout(currentLatitude, currentLongitude);
    }

     //开启跑步运动后记录轨迹
    private void beginService() {
        //没有开启gps打开dialog
        if (!Utils.isGpsOPen(this)) {
            Utils.showDialog(this);
            return;
        }

        isRunning = true;

        bike_layout.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.routing));
        textview_time.setText(getString(R.string.bike_time));
        textview_distance.setText(getString(R.string.bike_distance));
        prompt.setText(getString(R.string.routing_prompt));
        prompt1.setVisibility(View.GONE);

        prompt.setVisibility(View.VISIBLE);
        bike_layout.setVisibility(View.VISIBLE);

        unlock.setVisibility(View.GONE);
        if (routeOverlay != null)
            routeOverlay.removeFromMap();

        btn_locale.setVisibility(View.GONE);
        end_route.setVisibility(View.VISIBLE);
        tv_sos.setVisibility(View.VISIBLE);
        mMapView.showZoomControls(false);
        mBaiduMap.clear();

        if (isServiceLive)
            mlocationClient.requestLocation();
        //开启服务跑步后， 开启RouteService记录运动过程中的路程、 时间、 步数等数据
        Intent intent = new Intent(this, RouteService.class);
        startService(intent);

        MyLocationConfiguration configuration = new MyLocationConfiguration(locationMode, true, mIconLocation);
        //设置定位图层配置信息，只有先允许定位图层后设置定位图层配置信息才会生


    }




    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时销毁定位
        mlocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;

        isFirstIn = true;
        Log.d("yuyang", "RunActivity------------onDestroy------------------");
    }

    //
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            //点击返回按钮，当预约单车界面在时， 取消预约
          if (end_route.getVisibility() == View.VISIBLE){
                  toastDialog1();
          }else{
              finish();
          }
         }
        return super.onKeyDown(keyCode, event);
    }

    public class RunlocationListener implements BDLocationListener{

        //定位请求回调接口
        private boolean isFirst = true;

        //定位请求回调函数，这里会得到实时位置信息

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (bdLocation == null)
                return;

            if (!isRunning) return;

            //"4.9E-324" 表示目前所处的环境(室内或者网络不佳的情况)造成无法获取到经纬度
            if ("4.9E-324".equals(String.valueOf(bdLocation.getLatitude())) || "4.9E-324".equals(String.valueOf(bdLocation.getLongitude()))){
                return;
            }

            double routeLat = bdLocation.getLatitude(); //获取经纬度信息
            double routeLng = bdLocation.getLongitude();

            //将获取的坐标经纬度封装起来
            RoutePoint routePoint = new RoutePoint();  //当前实时坐标
            routePoint.setRouteLat(routeLat);
            routePoint.setRouteLng(routeLng);

            if (routePoints.size() <= 2){
                routePoints.add(routePoint);
              }else{
                   RoutePoint lastPoint = routePoints.get(routePoints.size()-1); //取出最后一点的坐标

                if (routeLat == lastPoint.getRouteLat() && routeLng == lastPoint.getRouteLng()){  //当用户坐标未改变时间

                 }else{  //当用户坐标改变时，存储新的坐标点并画出两点之间的路径

                    LatLng lastLatLng = new LatLng(lastPoint.getRouteLat(), lastPoint.getRouteLng()); //列表中最后的坐标
                    LatLng currentLatLng = new LatLng(routeLat, routeLng);               //当前坐标

                    needDPoints.add(lastLatLng);
                    needDPoints.add(currentLatLng);

                    routePoints.add(routePoint);

                    handler.sendEmptyMessageDelayed(1,500);

                }
            } }
    }


       //定位广播接收器， 接受来自运动过程服务发送的广播， 更新运动过程中的实时后台数据
    //接受来自RouteService的数据发送
    public static class LocationReceiver extends BroadcastReceiver {

        public LocationReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //判断跑步活动是否在前台
            if (Utils.isTopActivity(context)) {
                 time = intent.getStringExtra("totalTime");
                 distance = intent.getStringExtra("totalDistance");
                 step = intent.getStringExtra("totalStep");
                // pointList = intent.getParcelableArrayListExtra("routePoint");  //获取用户运动过程中的坐标点
               // String routePointStr = intent.getStringExtra("routePoint");

              //获取到广播数据就刷新控件
                run_step.setText(step);
                run_time.setText(time);
                run_distance.setText(distance);
          } else {
                Log.d("yuyang", "RunActivity-------TopActivity---------false");
            }
        }
    }

    //结束跑步弹框
    protected void toastDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RunActivity.this);
        builder.setMessage("确认结束跑步吗？");
        builder.setTitle("提示");
        //点击确认后停止RouteService 运动服务
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(RunActivity.this, RouteService.class);
                StepDetector.CURRENT_SETP = 0;
                stopService(intent);   //结束跑步服务

                // 意思是清空当前Handler队列所有消息。因为不这样做会导致类释放后还有可能执行Handler的那个延迟消息。
                time = "";
                step = "";
                distance = "";
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    //按返回按钮
    protected  void toastDialog1(){


        AlertDialog.Builder builder = new AlertDialog.Builder(RunActivity.this);
        builder.setMessage("跑步中， 确定结束跑步吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("结束跑步", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(RunActivity.this, RouteService.class);
                StepDetector.CURRENT_SETP = 0;
                // 意思是清空当前Handler队列所有消息。因为不这样做会导致类释放后还有可能执行Handler的那个延迟消息。
                stopService(intent);
                isRunning = false;
            }
        });
         builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isRunning = false;
            }
        });
          builder.create().show();
       }


      //按紧急求助按钮
    protected  void toastDialog2(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(RunActivity.this);
        builder.setMessage("确定跳转到紧急求助界面吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("需要紧急求助", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int sos = 1;
               Intent intent = new Intent(RunActivity.this, SoSActivity.class);
               // Intent intent = new Intent(RunActivity.this, RouteService.class);
              //  StepDetector.CURRENT_SETP = 0;
                // 意思是清空当前Handler队列所有消息。因为不这样做会导致类释放后还有可能执行Handler的那个延迟消息。
                startActivity(intent);
                isRunning = false;
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isRunning = false;
            }
        });
        builder.create().show();
    }

   }

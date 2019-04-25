package com.yuyang.baiduguiji.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.base.BaseActivity;
import com.yuyang.baiduguiji.bean.RoutePoint;
import com.yuyang.baiduguiji.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class RouteDetailActivity extends BaseActivity {

    private MapView route_detail_mapview;
    BaiduMap routeBaiduMap;
    private BitmapDescriptor startBmp, endBmp, currentBmp;   //地图中的标志Marker图标
   private MylocationListener mlistener;
   LocationClient mlocationClient;
    TextView total_time, total_distance,total_step , tv_route_replay, tv_title;
    public ArrayList<RoutePoint> routePoints;    //运动过程中记录的坐标集合
    public static boolean completeRoute = false;
    String time, distance, routePointsStr;
    RelativeLayout replay_progress_layout, route_mapview_layout;
    List<LatLng> points, subList;
    int routePointsLength, currentIndex = 0, spanIndex = 0;  //spanIndex根据运动记录中坐标集合总数而设定
    boolean isInReplay = false, pauseReplay = false;
    ImageView img_replay;
    SeekBar seekbar_progress;
    TextView tv_current_time, tv_current_speed;
    final int UPDATE_PROGRESS=1;

      //主线程中的handler， Hanlder每一秒刷新一次
     Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                //改变progress bar的状态
                case UPDATE_PROGRESS:
                    currentIndex = currentIndex + spanIndex; //当前的长度等于当前加增量，每一秒钟增加一个spanIndex
                    Log.d("yuyang", "currentIndex------------" + currentIndex);
                    routeBaiduMap.clear();
                    //当currentIndex还没达到记录中坐标集合数
                    if(currentIndex<routePointsLength)
                        //subList 为LatLng集合，
                        //取出subList中的从第一个到当前元素之间的所有元素
                        //即subList为当前长度的地理位置集合
                        subList = points.subList(0, currentIndex);
                      //当subList 大于2
                    if (subList.size() >= 2) {
                        //在地图画线的线样式
                        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                                .color(0xFF36D19D).points(subList);  //将subList集合中的坐标点连成线
                        //在地图上画线
                        routeBaiduMap.addOverlay(ooPolyline);  //将线显示在地图中
                    }

                    if (subList.size() >= 1) {  //
                        LatLng latLng = points.get(subList.size() - 1);  //得到坐标集合中的最后一个坐标
                        MarkerOptions options = new MarkerOptions().position(latLng)
                                .icon(currentBmp);     //运动图标
                        // 在地图上添加Marker，并显示
                        routeBaiduMap.addOverlay(options);   //将Marker 显示在地图上
                        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);  //将最新的一个坐标设置为地图状态
                        // 移动到某经纬度
                        routeBaiduMap.animateMapStatus(update);  //将地图位置更新到最新的状态中
                    }
                    //当前的
                    if (currentIndex < routePointsLength) {
                        tv_current_time.setText(Utils.getDateFromMillisecond(routePoints.get(currentIndex).time));
                        tv_current_speed.setText(routePoints.get(currentIndex).speed + "km/h");
                        int progress = (int) currentIndex * 100 / routePointsLength;
                        seekbar_progress.setProgress(progress);

                        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    } else {
                        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                                .color(0xFF36D19D).points(points);
                        routeBaiduMap.addOverlay(ooPolyline);
                        seekbar_progress.setProgress(100);   //seekbar最大数为100
                        handler.removeCallbacksAndMessages(null);
                        Toast.makeText(RouteDetailActivity.this, "轨迹回放结束", Toast.LENGTH_LONG).show();
                    }
            }

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        route_detail_mapview = (MapView) findViewById(R.id.route_detail_mapview);
        total_time = (TextView) findViewById(R.id.total_time);
        total_distance = (TextView) findViewById(R.id.total_distance);
        total_step = (TextView) findViewById(R.id.total_step);
        tv_route_replay = (TextView) findViewById(R.id.tv_route_replay);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        tv_current_speed = (TextView) findViewById(R.id.tv_current_speed);
        img_replay = (ImageView) findViewById(R.id.img_replay);
        seekbar_progress = (SeekBar) findViewById(R.id.seekbar_progress);
        replay_progress_layout = (RelativeLayout) findViewById(R.id.replay_progress_layout);
        route_mapview_layout = (RelativeLayout) findViewById(R.id.route_mapview_layout);
        route_mapview_layout.requestDisallowInterceptTouchEvent(true);
        route_detail_mapview.requestDisallowInterceptTouchEvent(true);
        routeBaiduMap = route_detail_mapview.getMap();
        route_detail_mapview.showZoomControls(false);
        startBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_start);
        endBmp = BitmapDescriptorFactory.fromResource(R.mipmap.route_end);
        currentBmp = BitmapDescriptorFactory.fromResource(R.mipmap.pb);
        setStatusBar();

        Intent intent = getIntent();  //当跑步结束后，记录跑步过程后台服务也随即结束， 从RouteService服务中发送一个Bundle将数据传送到RouteDetail
        //从Intent中获取各个字段属性值
        String time = intent.getStringExtra("totalTime");
        String distance = intent.getStringExtra("totalDistance");
        String stepcount = intent.getStringExtra("totalStep");
        routePointsStr = intent.getStringExtra("routePoints"); //从服务中获取的坐标集合
        routePoints = new Gson().fromJson(routePointsStr, new TypeToken<List<RoutePoint>>() {  //解析坐标json数据， 并将其封装到RoutePoint集合
        }.getType());//获取记录中所有的地理位置信息点
        //总记录的点数
        routePointsLength = routePoints.size();
/*
        每2s采集一个点，下面是实际行驶时间和轨迹回放时间的对应策略
*/
        //根据记录中的坐标总数，初始化spanIndex增量，
        //实际2～100s-->播放1～25s
        if (1 < routePointsLength && routePointsLength < 50) spanIndex = 2;
        //实际100~200s-->播放12~25s
        if (50 <= routePointsLength && routePointsLength < 100) spanIndex = 4;
        //实际200~1000s-->播放16~83s
        if (100 <= routePointsLength && routePointsLength < 500) spanIndex = 6;
        //实际1000~4000s-->播放62~250s
        if (500 <= routePointsLength && routePointsLength < 2000) spanIndex = 8;
        //实际4000~20000s-->播放166~833s
        if (2000 <= routePointsLength && routePointsLength < 10000) spanIndex = 12;
        //实际10000~20000s-->播放156s
        if (10000 <= routePointsLength ) spanIndex = 64;
        //在进入RouteDetailActivity画路径，完整的运动路径
        drawRoute();

        total_time.setText("跑步时长：" + time );
        total_distance.setText("跑步距离：" + distance );
        total_step.setText("跑步步数：" + stepcount );

        //设置拖动条监听
        seekbar_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 拖动条停止拖动的时候调用
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1);
            }

            /**
             * 拖动条开始拖动的时候调用
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //
                handler.removeCallbacksAndMessages(null);
            }

            /**
             * 拖动条进度改变的时候调用
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //progress为移动后的拖动条上的点坐标， 总数为100，
                //调整后重新设置currentIndex， 即改变地图上最后坐标的值
                //拖动时获取其当前的坐标点currentIndex键，
                currentIndex = (int) routePointsLength * progress / 100;
           }
        });

    }

    //在进入RouteDetailActivity画路径，完整的静态路径，
    public void drawRoute() {
        //points为地标集合
        points = new ArrayList<LatLng>();

        //将路径表中的坐标数据，拷贝到points中
        for (int i = 0; i < routePoints.size(); i++) {
            RoutePoint point = routePoints.get(i);
            LatLng latLng = new LatLng(point.getRouteLat(), point.getRouteLng());  //取出RoutePoint表中的位置数据
            points.add(latLng);  //添加到points
        }

         //将points中获取的坐标显示在map中
        if (points.size() > 2) {
            OverlayOptions ooPolyline = new PolylineOptions().width(10)
                    .color(0xFF36D19D).points(points);  //设置线的样式，并将点转化成线
            routeBaiduMap.addOverlay(ooPolyline);   //将线画在图上

            //接下来就是设置起点终点的Marker， 并将地图中点设置为起点坐标
            RoutePoint startPoint = routePoints.get(0);
            LatLng startPosition = new LatLng(startPoint.getRouteLat(), startPoint.getRouteLng());  //开始坐标

            //MapStatus 类通过MapStatus.Builder的方法设置中心点经纬度和地图。
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(startPosition).zoom(18.0f);
            //最后将设置好的属性装载到BaiduMap里面
            routeBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

            RoutePoint endPoint = routePoints.get(routePoints.size() - 1); //终点
            LatLng endPosition = new LatLng(endPoint.getRouteLat(), endPoint.getRouteLng());  //终点坐标
            //标注起点终点坐标
            addOverLayout(startPosition, endPosition);
        }
   }


      //位置监听接口
    public class MylocationListener implements BDLocationListener {
        //定位请求回调接口
        private boolean isFirstIn = true;

        //定位请求回调函数,这里面会得到定位信息
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //判断是否为第一次定位,是的话需要定位到用户当前位置
            if (isFirstIn) {
                Log.d("yuyang", "onReceiveLocation----------RouteDetail-----" + bdLocation.getAddrStr());
                isFirstIn = false;
                }
           }
    }

     //用标记标注起点与始点坐标
    private void addOverLayout(LatLng startPosition, LatLng endPosition) {

        // 定义Maker坐标点
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(startPosition)
                .icon(startBmp);  //将Marker设进MarkerOptions中  标注起始点坐标
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options);  //将起点坐标添加到图层
        MarkerOptions options2 = new MarkerOptions().position(endPosition)
                .icon(endBmp);   //标注终点坐标
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options2);  //将终点坐标添加到图层
 }

   /* private void addOverLayout2(LatLng startPosition, LatLng endPosition) {
        //先清除图层
        // mBaiduMap.clear();
        // 定义Maker坐标点
        // 构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions().position(startPosition)
                .icon(startBmp);
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options);
        MarkerOptions options2 = new MarkerOptions().position(endPosition)
                .icon(endBmp);
        // 在地图上添加Marker，并显示
        routeBaiduMap.addOverlay(options2);

    }*/

    public void onDestroy() {
        super.onDestroy();
         completeRoute = false;
    }

     //点击左上角返回按钮
    public void finishActivity(View view) {
        //当轨迹还在播放的时候，
        if (isInReplay) {
            //回到运动详情界面
            backFromReplay();
            return;
        }
        //播放轨迹结束
        completeRoute = true;
        //返回主界面
        finish();
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

    //点击暂停按钮执行的方法
    public void pauseReplay(View view) {
        //如果还未播放完毕
        if (!pauseReplay) {
            //运行的情况下点击
            //暂停播放轨迹， 即清除Handler需要处理的消息
            img_replay.setImageResource(R.mipmap.replay_stop);
            pauseReplay = true;
            handler.removeCallbacksAndMessages(null); //清空左右Handler消息
        } else {
            //暂停的情况下点击
            img_replay.setImageResource(R.mipmap.replay_start);
            pauseReplay = false;
            handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000); //继续发送Handler消息，每一秒刷新轨迹
        }
    }

    //点击“轨迹回放”
    public void startReplay(View view) {
        isInReplay = true;  //
        tv_title.setText(R.string.route_replay);
        tv_route_replay.setVisibility(View.GONE);
        routeBaiduMap.clear();  //将地图清空
        replay_progress_layout.setVisibility(View.VISIBLE);  //progressbar可见
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) route_mapview_layout.getLayoutParams();//获取布局参数属性
        params.height = Utils.getScreenHeight(this) - statusBarHeight - titleHeight;  //修改布局高度
        route_mapview_layout.setLayoutParams(params);  //重新设置高度

        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000); //每一秒progress bar滑动一次， 通过Handler将View的改变放置到主线程，同时播放轨迹路线
    }

      //轨迹回放或者点击返回按钮 调用此方法，回到运动详情界面
    public void backFromReplay() {
        isInReplay = false;
        tv_title.setText(R.string.route_detail);
        tv_route_replay.setVisibility(View.VISIBLE);
        routeBaiduMap.clear();
        //将subList清空
        subList.clear();
        currentIndex = 2;
        //重画运动详情中map轨迹
        drawRoute();

        replay_progress_layout.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) route_mapview_layout.getLayoutParams();
        params.height = Utils.dp2px(this, 240);   //设置运动详情界面高度属性值
        route_mapview_layout.setLayoutParams(params);

        handler.removeCallbacksAndMessages(null);//removeCallbacksAndMessages（null），
                                                          // 意思是清空当前Handler队列所有消息。因为不这样做会导致类释放后还有可能执行Handler的那个延迟消息。
    }

    //
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isInReplay) {
                backFromReplay();
                return false;
            }
            completeRoute = true;
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

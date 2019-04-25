package com.yuyang.baiduguiji.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.adapter.MyRouteAdapter;
import com.yuyang.baiduguiji.adapter.MyRouteDividerDecoration;
import com.yuyang.baiduguiji.base.BaseActivity;
import com.yuyang.baiduguiji.bean.RouteRecord;
import com.yuyang.baiduguiji.database.RouteDBHelper;

import java.util.ArrayList;
import java.util.List;

public class MyRouteActivity extends BaseActivity implements MyRouteAdapter.OnItemClickListener {

    XRecyclerView routeRecyclerView;
    MyRouteAdapter routeAdapter;
    List<RouteRecord> routeList;
    String TABLE_NAME = "cycle_route";  //表名
    int PageId = 0, PageSize = 10;
    long itemCount = 0;
    SQLiteDatabase db;
    TextView no_route;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route);
        //插件XRecyclerView为列表
        routeRecyclerView = (XRecyclerView) findViewById(R.id.recyclerview_route); //列表组件
        no_route = (TextView) findViewById(R.id.no_route);   //


        routeRecyclerView.setLayoutManager(new LinearLayoutManager(this));//给列表设置布局管理器
        setStatusBar();
        routeList = new ArrayList<RouteRecord>();  //声明运动记录类的集合

        RouteDBHelper helper = new RouteDBHelper(this);  //实例化数据库
        db = helper.getWritableDatabase();
        itemCount = getItemCount();  //cycle_route表中记录的条数
        routeList = loadPage();  //加载运动记录类的集合，加载界面首先显示10条最新的记录
        //获取数据后，将数据显示在XRecyclerView列表
        if (routeList != null) {
            routeAdapter = new MyRouteAdapter(this, routeList); //将RoutePoint集合 数据装载在适配器中
            routeRecyclerView.setAdapter(routeAdapter);  //给XRecyclerView列表设置适配器
            routeRecyclerView.addItemDecoration(new MyRouteDividerDecoration(10)); //给XRecyclerView分割线
            routeAdapter.setOnClickListener(this); //给适配器设置点击监听
        }else{
            //没有运动记录，显示no_route 控件
            Toast.makeText(MyRouteActivity.this, "没有运动记录！", Toast.LENGTH_SHORT).show();
            no_route.setVisibility(View.VISIBLE);
        }

        routeRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader); //设置刷新样式
        routeRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallScale);  //设置加载样式
        routeRecyclerView.setArrowImageView(R.drawable.iconfont_downgrey);
        routeRecyclerView.setPullRefreshEnabled(false); //关闭下拉刷新， 列表为下拉刷新


        //XRecyclerView 设置加载监听器
        routeRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {

            @Override
            public void onRefresh() {
//                Toast.makeText(MyRouteActivity.this, "onRefresh", Toast.LENGTH_SHORT).show();
                //下拉刷新
                routeRecyclerView.refreshComplete();
            }

            //每刷新一次列表， 获取新的10条数据
            @Override
            public void onLoadMore() {
//                Toast.makeText(MyRouteActivity.this, "onLoadMore", Toast.LENGTH_SHORT).show();
                //上滑加载数据库记录，
                loadPage(); //上滑一次加载10条数据
                routeRecyclerView.loadMoreComplete();
                routeAdapter.notifyDataSetChanged();  //更新适配器数据，刷新列表
            }
        });
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

    //点击列表项目跳转到RouteDetailActivity
    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(MyRouteActivity.this, RouteDetailActivity.class);
        RouteRecord routeRecord = routeList.get(position); //获取点击的列表项

        //两个activity之间的通讯可以通过bundle类来实现
        Bundle bundle = new Bundle();
        bundle.putString("totalTime", routeRecord.getCycle_time());
        bundle.putString("totalDistance", routeRecord.getCycle_distance());
        bundle.putString("totalStep", routeRecord.getCycle_step());
        bundle.putString("routePoints", routeRecord.getCycle_points());
        intent.putExtras(bundle);
        startActivity(intent);//跳转
    }

    /*
     * 读取指定ID的分页数据
     * SQL:Select * From TABLE_NAME Limit 9 Offset 10;
     * 表示从TABLE_NAME表获取数据，跳过10行，取9行
     */
    //根据cycle_route表记录数itemPage 创建 routeList
    //获取运动记录表
    public List<RouteRecord> loadPage() {

         //当routeList 临时RouteRecord记录数将cycle_route表
        //当程序中临时的RouteRecord记录数大于数据库中的记录数时
        if (routeList.size() >= itemCount) {
            routeRecyclerView.setNoMore(true);//  中数据加载完毕后， 设置routeRecyclerView .setNoMore(true)
            return null;
        }

        //PageSize为10
        //每次刷新获取十条记录， 避免数据过多查询时间过长
        String sql = "select * from " + TABLE_NAME + " order by route_id DESC" +
                " " + "limit " + String.valueOf(PageSize) + " offset " + PageId * PageSize;
        Cursor cursor = db.rawQuery(sql, null);

        //遍历查询到的记录，将记录封装到RouteRecord集合中
        while (cursor.moveToNext()) {
            RouteRecord point = new RouteRecord();  //实例化RouteRecord， 将cycle_route表中运动数据转移到RouteRecord 类
            //从数据库中获取记录集合， 根据记录字段获取各个字段参数， 并将其赋值给程序中临时的 RouteRecord类
            point.setCycle_date(cursor.getString(cursor
                    .getColumnIndex("cycle_date")));
            point.setCycle_time(cursor.getString(cursor
                    .getColumnIndex("cycle_time")));
            point.setCycle_distance(cursor.getString(cursor
                    .getColumnIndex("cycle_distance")));
            point.setCycle_step(cursor.getString(cursor
                    .getColumnIndex("cycle_step")));
            point.setCycle_points(cursor.getString(cursor
                    .getColumnIndex("cycle_points"))); //记录中地理坐标
            routeList.add(point);   //将RouteRecord类加入到routeList 集合中
        }


        PageId++; //页数自增
        cursor.close();  //关闭光标
        return routeList;  //返回RouteRecord 列表， 每次封装10条RouteRecord
    }

      //从cycle_route表中 获取记录条数
    public long getItemCount() {
        String sql = "select count(*) from " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();
        return count;
    }



    public void onDestroy() {
        super.onDestroy();

    }
}

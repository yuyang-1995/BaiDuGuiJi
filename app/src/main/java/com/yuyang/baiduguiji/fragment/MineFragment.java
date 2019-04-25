package com.yuyang.baiduguiji.fragment;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.activity.AboutActivity;
import com.yuyang.baiduguiji.activity.CompileDetailsActivity;
import com.yuyang.baiduguiji.activity.MinePlanActivity;
import com.yuyang.baiduguiji.activity.MyRouteActivity;
import com.yuyang.baiduguiji.base.BaseFragment;
import com.yuyang.baiduguiji.bean.RouteRecord;
import com.yuyang.baiduguiji.database.DatasDao;
import com.yuyang.baiduguiji.database.RouteDBHelper;
import com.yuyang.baiduguiji.util.SaveKeyValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import mrkj.library.wheelview.circleimageview.CircleImageView;


public class MineFragment extends BaseFragment implements View.OnClickListener {
    private static final int CHANGE = 200;
    private View view;//界面的布局
    private Context context;
    //上半部分
    private CircleImageView head_image;//显示头像
    private ImageButton change_values;//更改信息按钮
    private TextView custom_name;//用户名称
    private TextView want;
    //中间部分
    private LineChartView lineChartView;//统计图
    private LineChartData data;//数据集
    private float[] points = new float[7];//折线点的数组
    private DatasDao datasDao;//读取数据工具
    //private TextView show_steps;//显示今日已走的步数
    //下部分
    //private TextView food;//食物热量对照表
    private EditText steps;//步数
    private TextView about;//关于我们
    private TextView sport_message;//运动信息
    private TextView plan_btn;//计划

    private List<RouteRecord> routeList;
    private  ArrayList<Integer> stepList;
    String TABLE_NAME = "cycle_route";  //表名
    SQLiteDatabase db;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine, null);
        //1、第一部分显示头像、昵称
        head_image = (CircleImageView) view.findViewById(R.id.head_pic);
        custom_name = (TextView) view.findViewById(R.id.show_name);
        change_values = (ImageButton) view.findViewById(R.id.change_person_values);
        //点击跳转到编辑个人信息界面
        change_values.setOnClickListener(this);
        //2、第二部分显示当日的步数和历史统计图
       // show_steps = (TextView) view.findViewById(R.id.show_steps);
        lineChartView = (LineChartView) view.findViewById(R.id.step_chart);
        if (isAdded()) {
            datasDao = new DatasDao(getContext());
        }
        RouteDBHelper helper = new RouteDBHelper(this.getContext());  //实例化数据库
        db = helper.getWritableDatabase();
        routeList = new ArrayList<RouteRecord>();  //声明运动记录类集合
        stepList = new ArrayList<Integer>();  //声明集合
        //获取数据库中所有记录的步数
        routeList = loadRouteRecord();
        //将roteList数据撞到整型数组
        stepList = loadStepRecord();

        //显示信息
        showMessage();
        //3.初始化其余相关控件并添加点击事件的监听
       // food = (TextView) view.findViewById(R.id.food_hot);
     //   food.setOnClickListener(this);
        want = (TextView) view.findViewById(R.id.want);
     //   want.setText("在" + SaveKeyValues.getStringValues("plan_stop_date","2016年6月16日")+"体重达到【"+SaveKeyValues.getIntValues("plan_want_weight_values",50)+"】公斤");
        want.setText("当前体重为【" + SaveKeyValues.getIntValues("weight", 50) + "公斤】， 目标体重为【" + SaveKeyValues.getIntValues("plan_want_weight_values",50) + "】公斤") ;

        about = (TextView) view.findViewById(R.id.about_btn);
        about.setOnClickListener(this);
        sport_message = (TextView) view.findViewById(R.id.sport_btn);
        sport_message.setOnClickListener(this);
        steps = (EditText) view.findViewById(R.id.change_step);
        steps.setText(SaveKeyValues.getIntValues("step_plan" , 6000) + "");
        plan_btn = (TextView) view.findViewById(R.id.plan_btn);
        plan_btn.setOnClickListener(this);
        return view;
    }
        //从
    private ArrayList<Integer> loadStepRecord() {

        for (int i=0; i<routeList.size(); i++){
            RouteRecord routeRecord = routeList.get(i);
            String step = routeRecord.getCycle_step().trim();
            String[] steps = step.split("步");
            String st = steps[0];
            Log.e("步数", st);
            int stepss = Integer.parseInt(st);
            Integer in = Integer.valueOf(stepss);
            stepList.add(in);
     }
        return stepList;
    }

    //获取数据库中最近7条步数记录
    private List<RouteRecord> loadRouteRecord() {

     //   String sql = "select * from " +TABLE_NAME + "order by route_id DESC" + " "+" limit 6 offset 0";
        String sql = "select * from " + TABLE_NAME + " order by route_id DESC" +
                " " + "limit " + 7 + " offset " + 0;
        Cursor cursor = db.rawQuery(sql, null);

        //
        while(cursor.moveToNext()){
            RouteRecord routeRecord = new RouteRecord();
            routeRecord.setCycle_step(cursor.getString(cursor.getColumnIndex("cycle_step")));
            routeList.add(routeRecord);
        }
        cursor.close();
        int num = routeList.size();
        Log.e("长度：",num + " ");
        return  routeList;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!"".equals(steps.getText().toString())){
            SaveKeyValues.putIntValues("step_plan",Integer.parseInt(steps.getText().toString()));
        }else {
            SaveKeyValues.putIntValues("step_plan",6000);
        }
    }

    /**
     * 点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_person_values:
                startActivityForResult(new Intent(context, CompileDetailsActivity.class), CHANGE);
                Toast.makeText(context, "跳转到编辑个人信息界面！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_btn:
                startActivity(new Intent(context, AboutActivity.class));
                break;
            case R.id.sport_btn:
                startActivity(new Intent(context, MyRouteActivity.class));
                break;
            case R.id.plan_btn:
                startActivity(new Intent(context, MinePlanActivity.class));
                break;
            /*case R.id.food_hot:
                startActivity(new Intent(context, FoodHotListActivity.class));
                break;*/
            default:
                break;
        }
    }

    /**
     * 显示上部分和显示上部分
     */
    public void showMessage() {
        //上
        String name = SaveKeyValues.getStringValues("nick", "未填写");//获取名称
        String image_path = SaveKeyValues.getStringValues("path", "path");//获取图片路径
        //设置显示和功能
        custom_name.setText(name);
        if (!"path".equals(image_path)) {
            Bitmap bitmap = BitmapFactory.decodeFile(image_path);
            head_image.setImageBitmap(bitmap);
        }
        //中
        int today_steps = SaveKeyValues.getIntValues("sport_steps", 0);
        //show_steps.setText(today_steps + "步");
        //设置图表
        //获取保存的数据
       // Cursor cursor = datasDao.selectAll("step"); //查询步数表中所有的游标
        int counts = routeList.size();  //获取总游标的数量
        getDataValues(counts);  //根据游标查询所有的步数显示在折线图中
   }//

    private void getDateTest() {

        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel((i + 8) + "");
            axisValues.add(axisValue);
        }
        Axis axisx = new Axis();
        Axis axisy = new Axis();
        axisx.setTextColor(Color.BLACK)
                .setName("日期")
                .setValues(axisValues);
        axisy.setTextColor(Color.BLACK)
                .setName("步数")
                .setHasLines(true)
                .setMaxLabelChars(5);
        List<PointValue> values = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            points[i] = (int) (Math.random() * 1000 + 5000);
            values.add(new PointValue(i, points[i]));
            Log.e("运行" + "【" + i + "】", points[i] + "");
        }
        List<Line> lines = new ArrayList<>();
        Line line = new Line(values)
                .setColor(Color.parseColor("#4592F3"))
                .setCubic(false)
                .setHasPoints(false);
        line.setHasLines(true);
        line.setHasLabels(true);
        line.setHasPoints(true);
        lines.add(line);
        data = new LineChartData();
        data.setLines(lines);
        data.setAxisYLeft(axisy);
        data.setAxisXBottom(axisx);
        lineChartView.setLineChartData(data);

    }//

    /**
     * 绘制折线图
     * @param count
     */
    //绘制折线图
    private void getDataValues(int count) {

        //用来做X轴的标签
        //Calendar calendar = Calendar.getInstance();
        //int day = calendar.get(Calendar.DAY_OF_MONTH);//获取当前日期
        //点的集合
        List<PointValue> list = new ArrayList<>();
        int[] dataArray = {1,2,3,4,5,6,7};
        Log.e("总共记录条数",""+ count);
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i=0; i < count; i++){
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(dataArray[i] + "");
            axisValues.add(axisValue);
            }

            Axis axisx = new Axis(); //X轴
            Axis axisy = new Axis();  //Y轴

        axisx.setTextColor(Color.BLACK).setName("最近").setValues(axisValues);

        axisy.setTextColor(Color.BLACK).setName("步数").setHasLines(true).setMaxLabelChars(5);

        //设置各个数据点的步数
        for (int i=0; i<count; i++){
            int step = stepList.get(i);
            list.add(new PointValue(i,step));
        }

        List<Line> lines = new ArrayList<>();
        Line line = new Line(list).setColor(Color.parseColor("#4592F3")).setCubic(false).setHasPoints(false);

        line.setHasLabels(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        lines.add(line);

        data = new LineChartData();
        data.setLines(lines);
        data.setAxisYLeft(axisy);
        data.setAxisXBottom(axisx);



        lineChartView.setLineChartData(data);
    }

    /**
     * 这次后的六
     *
     * @param dateList
     */
  /*  private void getNestDayDate(int[] dateList, int k) {
        Calendar calendar = Calendar.getInstance();  //获取Calendar对象
        for (int i = k; i >= 0; i--) {
            calendar.add(Calendar.DATE, -1);   //获取当天的前一天的日子
            dateList[i] = calendar.get(Calendar.DAY_OF_MONTH);
        }
    }*/

    /**
     * 返回
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE && resultCode == Activity.RESULT_OK) {
            showMessage();
            Log.e("返回", "success");
        }
    }
}

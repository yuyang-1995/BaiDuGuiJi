package com.yuyang.baiduguiji.base;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuyang.baiduguiji.R;

import java.lang.reflect.Field;

import static com.yuyang.baiduguiji.util.Utils.dp2px;

public abstract class BaseActivity extends AppCompatActivity {

    public int statusBarHeight = 0,titleHeight;

    //
    private TextView title_center;//标题的中间部分
    private ImageView title_left,title_right;//标题的左边和右边
    private RelativeLayout title_relRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //抽象方法子类必须得继承
        getLayoutToView();   //初始化窗口
        initValues();         //设置初始化的值和变量
        setActivityTitle();//初始化标题
        initViews();        //初始化控件
        setViewsListener();    //初始化控件的监听
        setViewsFunction();       //设置相关管功能

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }*/
        statusBarHeight = getStatusBarHeight();
        titleHeight=dp2px(this,50);
    }

    /**
     * 设置沉浸式状态栏
     */
    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final ViewGroup linear_bar = (ViewGroup) findViewById(R.id.title_layout);
            final int statusHeight = getStatusBarHeight();
            linear_bar.post(new Runnable() {
                @Override
                public void run() {
//                    int titleHeight = linear_bar.getHeight();
                    Log.d("yuyang","titleHeight--------"+titleHeight);
                    Log.d("yuyang","statusHeight--------"+statusHeight);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linear_bar.getLayoutParams();
                    params.height = statusHeight + titleHeight;
                    linear_bar.setLayoutParams(params);
                }
            });
        }
    }

    protected void setStatusBarLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final ViewGroup linear_bar = (ViewGroup) findViewById(R.id.title_layout);
            final int statusHeight = getStatusBarHeight();  //状态栏高度
            linear_bar.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) linear_bar.getLayoutParams();
                    params.height = statusHeight ;     //相对布局高度
                    linear_bar.setLayoutParams(params);
                }
            });
        }
    }

    /**
     * 获取状态栏的高度
     *
     * @return
     */
    protected int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void finishActivity(View view) {
        finish();
    } /**
     * 初始化标题
     */
    public void initTitle(){
        title_center = (TextView) findViewById(R.id.titles);
        title_left = (ImageView) findViewById(R.id.left_btn);
        title_right = (ImageView) findViewById(R.id.right_btn);
        title_left.setVisibility(View.INVISIBLE);
        title_right.setVisibility(View.INVISIBLE);
        title_relRelativeLayout = (RelativeLayout) findViewById(R.id.title_back);
    }

    //
    public void setMyBackGround(int color){
        title_relRelativeLayout.setBackgroundResource(color);
    }

    /**
     * 设置TextView的下滑线
     * @param view
     */
    public void setTextViewUnderLine(TextView view){
        Paint paint = view.getPaint();
        paint.setColor(getResources().getColor(R.color.btn_gray));//设置画笔颜色
        paint.setAntiAlias(true);//设置抗锯齿
        paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);//设置下滑线
        view.invalidate();
    }
    /**
     * 初始化标题
     */
    protected abstract void setActivityTitle();

    /**
     * 初始化窗口
     */
    protected abstract void getLayoutToView();

    /**
     * 设置初始化的值和变量
     */
    protected abstract void initValues();
    /**
     * 初始化控件
     */
    protected abstract void initViews();

    /**
     * 初始化控件的监听
     */
    protected abstract void setViewsListener();

    /**
     * 设置相关管功能
     */
    protected abstract void setViewsFunction();

    /**
     * 设置标题的名称
     * @param name
     */
    public void setTitle(String name){
        title_center.setText(name);
        title_left.setVisibility(View.INVISIBLE);
    }


    /**
     * 设置标题有返回键功能-->可以改变返回键的图片
     * @param name
     * @param activity
     */
    public void setTitle(String name,final Activity activity){
        title_center.setText(name);
        title_left.setVisibility(View.VISIBLE);
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    /**
     * 获取标题左边的按钮
     * @param name
     * @return
     */
    public ImageView setTitleLeft(String name){
        title_center.setText(name);
        title_left.setVisibility(View.VISIBLE);
        return title_left;
    }


    /**
     * 设置标题左 中 右 全部显示
     * @param name
     * @param activity
     * @param picID
     */
    public ImageView setTitle(String name,final Activity activity ,int picID){
        title_center.setText(name);
        title_left.setVisibility(View.VISIBLE);
        title_right.setVisibility(View.VISIBLE);
        if (picID != 0){
            title_right.setImageResource(picID);
        }
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        return title_right;
    }

    /**
     * 设置标题的文字颜色
     * @param colorID
     */
    public void setTitleTextColor(int colorID){
        title_center.setTextColor(colorID);
    }

    /**
     * 设置标题左侧图片按钮的图片
     * @param picID
     */
    public void setTitleLeftImage(int picID){
        title_left.setImageResource(picID);
    }

    /**
     * 设置标题右侧图片按钮的图片
     * @param picID
     */
    public void setTitleRightImage(int picID){
        title_right.setImageResource(picID);
    }
}

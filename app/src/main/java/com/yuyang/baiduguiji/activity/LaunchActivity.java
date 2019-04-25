package com.yuyang.baiduguiji.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.util.Constant;
import com.yuyang.baiduguiji.util.SaveKeyValues;

import java.text.SimpleDateFormat;
import java.util.Date;

//程序启动的首个活动， 为欢迎界面， 跳转之后系统转到FunactionActivity， 设置受碎片为SportFragment
 public class LaunchActivity extends AppCompatActivity {

    private boolean isFirst;//是否为第一次启动

       private TextView tv_version;
       private TextView tv_go;
       private TextView tv_time;
       public String str;

    //主线称中实例化一个Handler
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                if (isFirst){
                    //如果应用为第一次启动 跳转到信息完善的界面
                    startActivity(new Intent(LaunchActivity.this, InformationActivity.class));
                }else {
                    //否则进入功能界面
                    startActivity(new Intent(LaunchActivity.this, FunctionActivity.class));
                }
                finish();
            }
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //不显示状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        initView();

        //设置默认加载运动首页
        SaveKeyValues.putIntValues("launch_which_fragment", Constant.TURN_MAIN);

        //第一次启动count为空 默认为0， 则isFirst 为true
        int count = SaveKeyValues.getIntValues("count" , 0);
        // count 为0 则isFirst = true
        isFirst = (count == 0)? true : false;
        //通过handler 发送一个 3秒的延时消息 3秒后活动跳转
        handler.sendEmptyMessageDelayed(1, 3000);

        //
        if(isFirst == false) {
            tv_go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LaunchActivity.this, FunctionActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }else {
            tv_go.setClickable(false);
        }


    }

       private void initView() {
           tv_version = (TextView)findViewById(R.id.tv_version);
           tv_go = (TextView) findViewById(R.id.tv_go);
           tv_time =(TextView) findViewById(R.id.tv_time);

           try {
               SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
               Date curDate = new Date(System.currentTimeMillis());
               str = format.format(curDate);
               tv_time.setText("Copyright" + " ©" + " 2018-12" + "～" + str);

               //获取程序包信息
               PackageInfo info =getPackageManager().getPackageInfo(getPackageName(), 0);

               tv_version.setText("V"+info.versionName);

           } catch (PackageManager.NameNotFoundException e) {

               e.printStackTrace();
               tv_version.setText("V");
       }
 }

       /**
     * 屏蔽返回键
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            return false;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(1);

    }
}

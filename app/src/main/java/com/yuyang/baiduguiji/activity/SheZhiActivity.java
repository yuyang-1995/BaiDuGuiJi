package com.yuyang.baiduguiji.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyang.baiduguiji.R;

public class SheZhiActivity extends AppCompatActivity implements View.OnClickListener{

    //选择天气背景
    private Button  btn_szback,btn_lainxiren;
    private TextView tv_savelxr;
   // private CheckBox checkBox;
    public static boolean cbisChecked;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



            //getWindow().setStatusBarColor(Color.parseColor("#106c99")); }
        setContentView(R.layout.activity_she_zhi);
        initView();
    }

    private void initView() {

       // btn_weatherbg = (Button) findViewById(R.id.btn_weatherbg);
        btn_szback = (Button) findViewById(R.id.btn_szback);
     //   checkBox = (CheckBox) findViewById(R.id.cb_ui);
        tv_savelxr = (TextView) findViewById(R.id.tv_savelxr);
        SharedPreferences pref = getSharedPreferences("lxr", MODE_PRIVATE);
        String lxrname = pref.getString("lxrname", "");
        if (lxrname != null && !lxrname.isEmpty()) {
            tv_savelxr.setText( "(*^_^*)"+"您已将" + lxrname + "设置为紧急联系人");
        }else {
            tv_savelxr.setText("紧急联系人为空，您可以点击左侧按钮进行设置");
        }

        // tv_lianxiren = (TextView) findViewById(R.id.tv_lianxiren);
        btn_lainxiren = (Button) findViewById(R.id.btn_lainxiren);

        SharedPreferences preferences = getSharedPreferences("cbox", MODE_PRIVATE);
        cbisChecked = preferences.getBoolean("ischecked", true);

        /*if (cbisChecked) {
            btn_weatherbg.setBackgroundColor(Color.parseColor("0xFF808080"));
            btn_weatherbg.setClickable(false);
            btn_weatherbg.setText("已开启系统自适应背景");
            checkBox.setChecked(true);
        }else {
            btn_weatherbg.setClickable(true);
            btn_weatherbg.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn));
            btn_weatherbg.setText("选择背景图片");
      //      btn_weatherbg.setOnClickListener(this);
            checkBox.setChecked(false);
        }*/
        btn_szback.setOnClickListener(this);
        btn_lainxiren.setOnClickListener(this);


/*        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    cbisChecked = true;
                    SharedPreferences.Editor editor = getSharedPreferences("cbox", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.putBoolean("ischecked", cbisChecked);
                    editor.apply();
                   *//* btn_weatherbg.setClickable(false);
                    btn_weatherbg.setBackgroundColor(0xFF808080);
                    btn_weatherbg.setText("已开启系统自适应背景");
                    Toast.makeText(SheZhiActivity.this, "下拉刷新界面启动自适应背景图", Toast.LENGTH_SHORT).show();*//*
                }  else {
                    btn_weatherbg.setClickable(true);
                    cbisChecked = false;
                    SharedPreferences.Editor editor = getSharedPreferences("cbox", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.putBoolean("ischecked", cbisChecked);
                    editor.apply();
                    btn_weatherbg.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn));
                    btn_weatherbg.setText("选择背景图片");
                }
            }
        });*/

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
          /*  case R.id.btn_weatherbg:
                Intent intent = new Intent(ShezhiActivity.this, XuanZeBgActivity.class);
                startActivity(intent);
                break;
*/
            case R.id.btn_szback:
                Intent intent1 = new Intent(SheZhiActivity.this, FunctionActivity.class);
                startActivity(intent1);
                break;
           case R.id.btn_lainxiren:
                Intent intent2 = new Intent(SheZhiActivity.this, LianXiRenActivity.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            Intent intent = new Intent(SheZhiActivity.this, FunctionActivity.class);
            startActivity(intent);
        }
        return true;
    }
}

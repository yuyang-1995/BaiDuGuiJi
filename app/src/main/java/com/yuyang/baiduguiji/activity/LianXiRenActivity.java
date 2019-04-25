package com.yuyang.baiduguiji.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyang.baiduguiji.R;

public class LianXiRenActivity extends AppCompatActivity  implements View.OnClickListener {
    private Button btn_lxrback;
    private EditText et_yourname,et_lxrname,et_lxrnumber;
    private TextView tv_lxrsave,tv_tishia;
    private static String yourname = "", lxrname = "", lxrnumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {   //设置透明

            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            getWindow().setStatusBarColor(Color.TRANSPARENT); }
        setContentView(R.layout.activity_lian_xi_ren);

        initView();
        initData();


    }

    private void initData() {
        btn_lxrback.setOnClickListener(this);
        et_yourname.setOnClickListener(this);
        et_lxrname.setOnClickListener(this);
        et_lxrnumber.setOnClickListener(this);
        tv_lxrsave.setOnClickListener(this);


    }

    private void initView() {

        btn_lxrback = (Button) findViewById(R.id.btn_lxrback);
        et_yourname = (EditText) findViewById(R.id.et_yourname);
        et_lxrname = (EditText) findViewById(R.id.et_lxrname);
        et_lxrnumber = (EditText) findViewById(R.id.et_lxrnumber);
        tv_lxrsave = (TextView) findViewById(R.id.tv_lxrsave);
        tv_tishia = (TextView) findViewById(R.id.tv_tishia);

        tv_tishia.setText(" 注：①：为了此功能的正常工作，请填写真实有效的姓名和号码。 " + "\n \n        ②：保障您的隐私安全，所填写的信息和数据只会记录在手机本地。" + "\n  \n        ③：点击修改可更换紧急联系人"
        );

        SharedPreferences pref = getSharedPreferences("lxr", MODE_PRIVATE);
        String yn = pref.getString("yourname", "");
        String ln = pref.getString("lxrname", "");
        String lnum = pref.getString("lxrnumber", "");
        if ( !yn.isEmpty() && !ln.isEmpty() && !lnum.isEmpty()) {
            et_yourname.setEnabled(false);
            et_lxrname.setEnabled(false);
            et_lxrnumber.setEnabled(false);

            et_yourname.setText(yn + "(您的姓名)");
            et_lxrname.setText(ln + "(紧急联系人名字)");
            et_lxrnumber.setText(lnum + "(紧急联系人号码)");
            tv_lxrsave.setText("修改");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.et_lxrname:
                Intent intent = new Intent(LianXiRenActivity.this, SheZhiActivity.class);
                startActivity(intent);
                break;
            default:
                break;

            case R.id.tv_lxrsave:
                //当点击修改后， et 可编辑 清除et tv_lxrsave设置为保存，
                if (tv_lxrsave.getText().equals("修改")){
                    et_yourname.setText("");
                    et_lxrname.setText("");
                    et_lxrnumber.setText("");
                    et_yourname.setEnabled(true);
                    et_lxrname.setEnabled(true);
                    et_lxrnumber.setEnabled(true);
                    tv_lxrsave.setText("保存");
                }

                //当点击保存后， editor保存数据 当数据不完整时 不可保存并且提示用户
                if (tv_lxrsave.getText().equals("保存")){

                    yourname = String.valueOf(et_yourname.getText()).trim();
                    lxrname = String.valueOf(et_lxrname.getText()).trim();
                    lxrnumber = String.valueOf(et_lxrnumber.getText()).trim();
                    //不能保存
                    if (yourname.isEmpty() || lxrname.isEmpty() || lxrnumber.isEmpty() || (lxrnumber.length() != 11)  ) {
                        yourname = ""; lxrname = ""; lxrnumber = "";
                        Toast.makeText(LianXiRenActivity.this, "请完整有效地填写所需信息", Toast.LENGTH_SHORT).show();
                    }
                    //可以保存
                    else {
                        tv_lxrsave.setText("修改");
                        et_yourname.setEnabled(false);
                        et_lxrname.setEnabled(false);
                        et_lxrnumber.setEnabled(false);
                        et_yourname.setText(yourname + "(您的姓名)");
                        et_lxrname.setText(lxrname + "(紧急联系人姓名)");
                        et_lxrnumber.setText(lxrnumber + "(紧急联系人号码)");

                        SharedPreferences.Editor editor = getSharedPreferences("lxr", MODE_PRIVATE).edit();
                        editor.putString("yourname", yourname);
                        editor.putString("lxrname", lxrname);
                        editor.putString("lxrnumber", lxrnumber);
                        editor.apply();
                        Toast.makeText(LianXiRenActivity.this, "设置成功", Toast.LENGTH_SHORT).show();

                    } }
                break;


            case R.id.btn_lxrback:
                Intent intent1 = new Intent(LianXiRenActivity.this, SheZhiActivity.class);
                startActivity(intent1);
                break;


        }

    }
}

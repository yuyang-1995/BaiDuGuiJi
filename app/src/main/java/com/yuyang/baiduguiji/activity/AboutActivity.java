package com.yuyang.baiduguiji.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
//import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuyang.baiduguiji.R;

   //关于界面
  public class AboutActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv_jieshao, tv_yuanma, tv_fankui, tv_aabanben, tv_sosjiehsao;
    private ImageView iv_yuamma, iv_fankui;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {   //设置透明

            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

      //  CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        setSupportActionBar(toolbar);
        tv_fankui = (TextView) findViewById(R.id.tv_fankui);
        tv_yuanma = (TextView) findViewById(R.id.tv_yuanma);
        iv_fankui = (ImageView) findViewById(R.id.iv_fankui);
        iv_yuamma = (ImageView) findViewById(R.id.iv_yuanma);
        tv_aabanben = (TextView) findViewById(R.id.tv_aabanben);
        tv_sosjiehsao = (TextView) findViewById(R.id.tv_sosjiehsao);

        try {
            PackageInfo info =getPackageManager().getPackageInfo(getPackageName(), 0);
            tv_aabanben.setText( "StayHealthy " + info.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        tv_yuanma.setOnClickListener(this);
        tv_fankui.setOnClickListener(this);
        iv_fankui.setOnClickListener(this);
        iv_yuamma.setOnClickListener(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    //    collapsingToolbarLayout.setTitle("StayHealthy");

        tv_jieshao = (TextView) findViewById(R.id.tv_jieshao);
        tv_jieshao.setText("  StayHealthy是一款基于安卓平台的即时天气信息获取软件，其采用的技术使其具有以下主要功能及特点：\n" +
                "   ①:多城市天气信息的即时获取及查看。\n " +
                "  ②:设定自己喜欢的内置图片作为天气背景图。\n " +
                "  ③:跟据所在季节和天气信息匹配合适的背景图。\n " +
                "  ④:手动刷新及后台自动刷新天气数据。\n " +
                "  ⑤:SOS功能模块让你的生活多一份的安全感。\n " +

                "  注:由于开发者当前技术上的短板以及开发周期和资源的限制 使得软件或多或少有功能上的缺陷，UI上的不足，逻辑代码上的bug; 希望得到您的反馈和建议。\n \n   另：为了软件所带功能的正常有效使用，请允许软件访问设备地理位置发送短信和通讯录数据等权限。\n" );

        tv_sosjiehsao.setText("  SOS模块是此App的一大特点其主要有两大功能- \n 一：快速一键式地向联系人发送求救短信，使联系人获取您所需要的帮助和地理位置等信息。\n \n二：模仿微信等社交工具的语言聊天，内置多个语音片段，如坐网约车遇到危险 可参考着使用此功能。 \n"
                + "  为了更好的使用SOS功能模块, 需要注意以下几点： \n "
                + "   ①:为了使软件为您提供有效的服务，允许软件访问您的通讯录 发送短信及地理位置等权限(这些数据只会在手机本地被使用)。 \n \n"
                + "   ②:在发送短信之前， 您可以选取一些内置的文字标签，所选的文字标签会作为短信内容发送给联系人，以便您得到更好的帮助。 \n \n"
                + "   ③:点击“短信速发”界面右上角的图标可给紧急联系人发送短信， 前提是您需要在设置中添加“紧急联系人”。 \n"
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_yuanma:
                jiehsouyuanma();

                break;

            case R.id.tv_fankui:

                startSendEmail();
                break;

            default:
                break;
        };
    }

    private void jiehsouyuanma() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://pan.baidu.com/s/1VpF7qIbL_qPYU55zM3Of1Q"));
        startActivity(intent);
     }

    private void startSendEmail() {
        String address = "1335367655@qq.com";
        String subject = "“StayHealthy”用户反馈";
        String body = "给花椒天气的建议：";
        String contnet = "mailto:" + address + "?subject=" + subject + "&body=" + body;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(contnet));
        startActivity(Intent.createChooser(intent, "反馈或建议"));
    }
}

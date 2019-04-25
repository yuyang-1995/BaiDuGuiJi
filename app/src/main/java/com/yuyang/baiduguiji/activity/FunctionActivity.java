package com.yuyang.baiduguiji.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.base.BaseActivity;
import com.yuyang.baiduguiji.fragment.FindFragment;
import com.yuyang.baiduguiji.fragment.HeartFragment;
import com.yuyang.baiduguiji.fragment.MineFragment;
import com.yuyang.baiduguiji.fragment.SportFragment;
import com.yuyang.baiduguiji.util.AllInterface;
import com.yuyang.baiduguiji.util.Constant;

import com.yuyang.baiduguiji.util.NetworkUtils;
import com.yuyang.baiduguiji.util.SaveKeyValues;


   //四个碎片所存在的界面
public class FunctionActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, AllInterface.OnMenuSlideListener {

    //变量
    private long exitTime;//第一次单机退出键的时间
    private int load_values;//判断加载fragment的变量
    //控件
    private RadioGroup radioGroup;//切换按钮的容器
    private RadioButton sport_btn,find_btn,heart_btn,mine_btn;//切换按钮
    //碎片
    private SportFragment sportFragment;//运动
    private FindFragment findFragment;//发现
    private HeartFragment heartFragment;//心率
    private MineFragment mineFragment;//我的

       private AlertDialog alertDialog;  //无网络或没开启GPS

       //滑动菜单
       private DrawerLayout mDrawerLayout;

       //菜单内容
       private NavigationView navigationView;

    /**
     * 设置标题
     */
    @Override
    protected void setActivityTitle() {

    }

    /**
     * 初始化界面
     */
    @Override
    protected void getLayoutToView() {
        setContentView(R.layout.activity_function);
    }

    /**
     * 初始化相关变量
     */
    @Override
    protected void initValues() {

        //如果这个值等于1就加载运动界面，等于2就加载发现界面
        load_values = SaveKeyValues.getIntValues("launch_which_fragment",0);
        Log.e("加载判断值", load_values + "");
        //实例化四个碎片相关碎片
        sportFragment = new SportFragment();
        findFragment = new FindFragment();

        heartFragment = new HeartFragment();
         mineFragment = new MineFragment();

         boolean isNetWork;
          isNetWork = NetworkUtils.isNetWorkAvailable(this);

          //  alertDialog = new AlertDialog.Builder(SoSActivity.this).create();
        //
        //                alertDialog.show();
        //                final Window window = alertDialog.getWindow();
        //                window.setContentView(R.layout.alertdialog);
        //                TextView tv_dailogtitle = (TextView) window.findViewById(R.id.tv_dailogtitle);
        //                tv_dailogtitle.setText("SOS-短信速发");
        //                TextView tv_dialogmessage = (TextView) window.findViewById(R.id.tv_dialogmessage);
        //                tv_dialogmessage.setText("  点击“发送”即给联系人 " + lxrname +"(" + lxrnumber + ")"+" 发送紧急短信。(注:请勿在非危急或非必要的条件下使用此功能，以免对您产生不必要的损失)。"
        //                        + "\n \n 联系人-"  + lxrname + "将收到以下内容的短信。"
        //                        + "\n \n   【此短信为"+ yourname +"在危急情况下通过软件给您发送的紧急短信，请您在收到短信后立即与他(她)进行联系、或跟据短信所提供的信息采取其他必要措施，在与 " + yourname +" 取得联系并确认其安全之前 " +
        //                        "请对其动态及状况持续保持关注。(注：下面的信息均为" + yourname +"发送短信时给您提供的信息)"
        //                        + "\n \n "+ yourname +"发送信息时的位置："  +  tv_cityloc.getText()
        //                        + "\n"+ yourname +" 所处的环境："  + huanjin
        //                        + "\n"+ yourname +" 所处的状态： " + zhuangtai
        //                        + "\n "+ yourname +"需要您提供的帮助: " + xuyao1 + "," + xuyao2 + "," + xuyao3
        //                        + "】" );
         if (!isNetWork || !isGps()){
              alertDialog = new  AlertDialog.Builder(FunctionActivity.this).create();
              alertDialog.show();
             Window window = alertDialog.getWindow();
             window.setContentView(R.layout.alertdialog);
             TextView tv_title = window.findViewById(R.id.tv_dailogtitle);
             tv_title.setText("敬告:");
             TextView tv_message = window.findViewById(R.id.tv_dialogmessage);
             tv_message.setText("     无网络或GPS信号， 请确认处于网络连接或开启GPS后开启应用。");

             TextView tv_queren = window.findViewById(R.id.tv_alok);
             tv_queren.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     finish();
                     alertDialog.dismiss();
                 }
             });

             TextView tv_alcancel = (TextView) window.findViewById(R.id.tv_alcancel);
             tv_alcancel.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     finish();
                     alertDialog.dismiss();
                 }
             });
    }


        //初始化界面
        if (load_values == Constant.TURN_MAIN){
            //Bundle 经常用于Activity之间传递数据
            Bundle bundle = new Bundle();
            bundle.putBoolean("is_launch",true);  //以key-value(键值对)的形式存在的
            sportFragment.setArguments(bundle);   //通过Fragment.setArguments(Bundle bundle)方法设置的bundle会保留下来
            //getSupportFragmentManager().beginTransaction().add(R.id.frag_home,sportFragment,Constant.SPORT_TAG).commit();
            //在FrameLayout 中添加碎片
            getSupportFragmentManager().beginTransaction().add(R.id.frag_home, sportFragment, Constant.SPORT_TAG).commit();
        }else {
            getSupportFragmentManager().beginTransaction().add(R.id.frag_home,findFragment,Constant.FIND_TAG).commit();
        }
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        radioGroup = (RadioGroup) findViewById(R.id.ui_btn_group);
        sport_btn = (RadioButton) findViewById(R.id.sport_btn);
        find_btn = (RadioButton) findViewById(R.id.find_btn);
        heart_btn = (RadioButton) findViewById(R.id.heart_btn);
        mine_btn = (RadioButton) findViewById(R.id.mine_btn);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.m_draw);  //侧滑栏菜单
        navigationView = (NavigationView) findViewById(R.id.nav_view); //菜单项布局

        //给侧滑栏菜单项设置点击监听
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_run_history:
                        Intent intent = new Intent(FunctionActivity.this, MyRouteActivity.class);
                        startActivity(intent);
                        break;
                    case  R.id.nav_about:
                        Intent intent1 = new Intent(FunctionActivity.this, AboutActivity.class);
                        startActivity(intent1);
                        break;
                    case  R.id.nav_shezi:
                        Intent intent2 = new Intent(FunctionActivity.this, SheZhiActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.nav_sos:
                        Intent intent3 = new Intent(FunctionActivity.this, SoSActivity.class);
                        startActivity(intent3);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

       //点击打开滑动菜单
       @Override
       public boolean onOptionsItemSelected(MenuItem item) {
           switch (item.getItemId()) {
               case R.id.menu_icon:
                   mDrawerLayout.openDrawer(GravityCompat.START);
                   break;
               default:
                   break;
           }
           return true;
       }

    /**
     * 设置监听
     */
    @Override
    protected void setViewsListener() {
        //给RadioGroup 注册选择监听
        radioGroup.setOnCheckedChangeListener(this);
    }

    /**
     * 设置功能
     */
    @Override
    protected void setViewsFunction() {
        if (load_values == Constant.TURN_MAIN){
            sport_btn.setChecked(true);
        }else {
            find_btn.setChecked(true);
        }
    }



    /**
     * 切换界面 注册RadioGroup监听后的回调方法
     * @param group
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (checkedId){
            case R.id.sport_btn://运动
                //isAdded() 方法判断碎片是否已经添加到活动中
                //如果活动中还没有此碎片， 就将其添加至活动布局
                if (!sportFragment.isAdded()){
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("is_launch", false);
                    sportFragment.setArguments(bundle);
                    transaction.replace(R.id.frag_home,sportFragment,Constant.SPORT_TAG);
                }
                break;
            case R.id.find_btn://发现
                if (!findFragment.isAdded()){
                    transaction.replace(R.id.frag_home, findFragment,Constant.FIND_TAG);
                }
                break;
            case R.id.heart_btn://心率
                if (!heartFragment.isAdded()){
                    transaction.replace(R.id.frag_home,heartFragment,Constant.HEART_TAG);
                }
                break;
            case R.id.mine_btn://我的
                if (!mineFragment.isAdded()){
                    transaction.replace(R.id.frag_home,mineFragment,Constant.MINE_TAG);
                }
                break;
            default:
                break;
        }
        //选择后提交事务
        transaction.commit();
    }

    /**
     * 按两次退出按钮退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            // System.currentTimeMillis()无论何时调用，肯定大于2000
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

       @Override
       public void onMenuSlide(float offset) {

       }


       private boolean isGps(){
        @SuppressLint("ServiceCast") LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

           boolean ok=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);


           return  ok;
       }

       //LocationManager alm =
       //(LocationManager)this.getSystemService( Context.LOCATION_SERVICE );
       //if( alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER ) )
       //---------------------
   }

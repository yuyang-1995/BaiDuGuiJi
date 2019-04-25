package com.yuyang.baiduguiji.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.adapter.LianxiAdapter;
import com.yuyang.baiduguiji.bean.Lianxiren;

import java.util.ArrayList;
import java.util.List;

public class SoSActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_sosback, btn_faduanxin;
    private TextView tv_cityloc,tv_huanjin1, tv_huanjin2,tv_huanjin3,tv_huanjin4,tv_huanjin5, tv_zhuangtai1,tv_zhuangtai2, tv_zhuangtai3, tv_xuyao1, tv_xuyao2, tv_xuyao3, tv_xuyao4, tv_xuyao5;

    private ListView lv_lianxiren;
    public String lxrname;
    public String lxrnumber;
    private String huanjin = "", zhuangtai = "", xuyao1 = "", xuyao2 = "", xuyao3 = "";

    private String duanxin = "", duanxin1="";
    private AlertDialog alertDialog, alertDialog1;

    private List<Lianxiren> lianxirenList = new ArrayList<>();

    private List<PendingIntent> sentIntents = new ArrayList<>();

    private String yourname;

    //广播接收器
    private BroadcastReceiver sendBroadcastReceiver, deliverBroadcastReceiver;

    String SENT_SMS_ACTION = "SENT_SMS_ACTION";

    String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";



    //定位
    private String locProvince, locCity, locCounty, locRoad, locMethed, locjindu, locweidu;

    private LocationClient mLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持屏幕不变黑

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_so_s);

        List<String> permissionList = new ArrayList<>();

        initView();
        initData();
        initLianxiren();// 初始化联系人
        initBroadcastReceiver(); //注册监听器

        LianxiAdapter adapter = new LianxiAdapter(SoSActivity.this, R.layout.lianxiren, lianxirenList);

        lv_lianxiren.setAdapter(adapter);

        lv_lianxiren.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                SharedPreferences preferences = getSharedPreferences("lxr", MODE_PRIVATE);
                yourname = preferences.getString("yourname", "");


                Lianxiren lianxiren = lianxirenList.get(i);
                lxrname = lianxirenList.get(i).getName();
                lxrnumber = lianxirenList.get(i).getNumber();
                //  AlertDialog.Builder dialog = new AlertDialog.Builder(SOSActivity.this );
                alertDialog = new AlertDialog.Builder(SoSActivity.this).create();

                alertDialog.show();
                final Window window = alertDialog.getWindow();
                window.setContentView(R.layout.alertdialog);
                TextView tv_dailogtitle = (TextView) window.findViewById(R.id.tv_dailogtitle);
                tv_dailogtitle.setText("SOS-短信速发");
                TextView tv_dialogmessage = (TextView) window.findViewById(R.id.tv_dialogmessage);
                tv_dialogmessage.setText("  点击“发送”即给联系人 " + lxrname +"(" + lxrnumber + ")"+" 发送紧急短信。(注:请勿在非危急或非必要的条件下使用此功能，以免对您产生不必要的损失)。"
                        + "\n \n 联系人-"  + lxrname + "将收到以下内容的短信。"
                        + "\n \n   【此短信为"+ yourname +"在危急情况下通过软件给您发送的紧急短信，请您在收到短信后立即与他(她)进行联系、或跟据短信所提供的信息采取其他必要措施，在与 " + yourname +" 取得联系并确认其安全之前 " +
                        "请对其动态及状况持续保持关注。(注：下面的信息均为" + yourname +"发送短信时给您提供的信息)"
                        + "\n \n "+ yourname +"发送信息时的位置："  +  tv_cityloc.getText()
                        + "\n"+ yourname +" 所处的环境："  + huanjin
                        + "\n"+ yourname +" 所处的状态： " + zhuangtai
                        + "\n "+ yourname +"需要您提供的帮助: " + xuyao1 + "," + xuyao2 + "," + xuyao3
                        + "】" );
                // duanxin =  "  此短信为余洋通过软件向您发送的紧急短信，请您在收到短信后立即与他(她)进行联系、或跟据短信内容采取其他有效的必要措施，在与余洋取得联系并确认其安全之前 "

                duanxin =  " 此短信为"+ yourname +  "在危急情况下通过手机软件给您发送的紧急短信，请您在收到短信后立即与他(她)进行联系、或跟据短信所提供的信息采取其他必要措施，在与 " + yourname +" 取得联系并确认其安全之前 " +
                        "请对其动态及状况持续保持关注。(注：下面的信息均为" + yourname +"发送短信时给您提供的信息)"
                        + "\n "+ yourname +"所处的位置：" + tv_cityloc.getText()
                        + "\n "+ yourname + "所处的环境：" + huanjin
                        + "\n "+ yourname + "所处的状态：" + zhuangtai
                        + "\n "+ yourname + "需要您提供的帮助:" + xuyao1  + "," + xuyao2 + "," + xuyao3;



                TextView tv_alcancel = (TextView) window.findViewById(R.id.tv_alcancel);
                tv_alcancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                TextView tv_alok = (TextView) window.findViewById(R.id.tv_alok);

                tv_alok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (duanxin != null){

                            SmsManager sms =  SmsManager.getDefault();

                            Intent sentintent = new Intent(SENT_SMS_ACTION);
                            PendingIntent sentPI = PendingIntent.getBroadcast(SoSActivity.this, 0, sentintent, 0);

                            Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
                            PendingIntent deliverPI = PendingIntent.getBroadcast(SoSActivity.this, 0, deliverIntent, 0);

                            if (duanxin.length() > 70) {
                                ArrayList<String> msgs = sms.divideMessage(duanxin);
                                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

                                for (int i = 0 ; i < msgs.size(); i++) {
                                    sentIntents.add(sentPI);
                                }
                                sms.sendMultipartTextMessage(lxrnumber, null, msgs, sentIntents, null);
                            }else {
                                sms.sendTextMessage(lxrnumber, null, duanxin,sentPI, deliverPI );

                            }

                        }
                        Toast.makeText(SoSActivity.this, "短信已在后台发送", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });

            }
        });

        if (ContextCompat.checkSelfPermission(SoSActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(SoSActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(SoSActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CONTACTS);
        }

        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(SoSActivity.this, permissions, 1);
        }else {
            requestLocation();
        }
    }


    //初始化广播接收器
    private void initBroadcastReceiver() {

        //发送短信状态
        IntentFilter sendIntentFilter = new IntentFilter();
        sendIntentFilter.addAction(SENT_SMS_ACTION);
        sendBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case SoSActivity.RESULT_OK:
                        Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(sendBroadcastReceiver, sendIntentFilter);

        //接收短信状态
        IntentFilter deliverIntentFilter = new IntentFilter();
        deliverIntentFilter.addAction(DELIVERED_SMS_ACTION);
        deliverBroadcastReceiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "收信人已经成功接受", Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(deliverBroadcastReceiver, deliverIntentFilter);
    }

    private void requestLocation() {
        initLocation(); //每个三秒更新位置
        mLocationClient.start();  //开始定位
    }

    //设置定位参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(3000); //setScanSpan设置更新间隔
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);

    }

    //定位监听
    public class  MyLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    locjindu = String.valueOf(bdLocation.getLongitude());
                    locweidu = String.valueOf(bdLocation.getLatitude());
                    locProvince = bdLocation.getProvince();
                    locCity = bdLocation.getCity();
                    locRoad = bdLocation.getStreet();
                    locCounty = bdLocation.getDistrict();

                    if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                        locMethed = "GPS";
                    } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                        locMethed = "网络";}
                    tv_cityloc.setText(locMethed + "定位在:" + locProvince + locCity + locCounty + locRoad);
                }
            });
        }
    }


    //申请运行时权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case  1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用此功能", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } requestLocation();
                }else  {
                    Toast.makeText(this, "发生位置错误", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }
    //初始化联系人
    private void initLianxiren() {
        Cursor cursor;

        //查询联系人数据
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        try {

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    //获取姓名
                    String  name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    //获取联系人电话
                    String  number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    Lianxiren lianxiren = new Lianxiren(name, number);
                    lianxirenList.add(lianxiren);

                }}
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void initData() {

        btn_sosback.setOnClickListener(this);
        tv_huanjin1.setOnClickListener(this);
        tv_huanjin2.setOnClickListener(this);
        tv_huanjin3.setOnClickListener(this);
        tv_huanjin4.setOnClickListener(this);
        tv_huanjin5.setOnClickListener(this);

        tv_zhuangtai1.setOnClickListener(this);
        tv_zhuangtai2.setOnClickListener(this);
        tv_zhuangtai3.setOnClickListener(this);

        tv_xuyao1.setOnClickListener(this);
        tv_xuyao2.setOnClickListener(this);
        tv_xuyao3.setOnClickListener(this);
        tv_xuyao4.setOnClickListener(this);
        tv_xuyao5.setOnClickListener(this);

        btn_faduanxin.setOnClickListener(this);


    }

    //初始化界面
    private void initView() {


        btn_sosback = (Button) findViewById(R.id.btn_sosback);
        tv_cityloc = (TextView) findViewById(R.id.tv_cityloc);
        lv_lianxiren = (ListView) findViewById(R.id.lv_lianxiren);
        tv_huanjin1 = (TextView) findViewById(R.id.tv_huanjin1);
        tv_huanjin2 = (TextView) findViewById(R.id.tv_huanjin2);
        tv_huanjin3 = (TextView) findViewById(R.id.tv_huanjin3);
        tv_huanjin4 = (TextView) findViewById(R.id.tv_huanjin4);
        tv_huanjin5 = (TextView) findViewById(R.id.tv_huanjin5);

        tv_zhuangtai1 = (TextView) findViewById(R.id.tv_zhuangtai1);
        tv_zhuangtai2 = (TextView) findViewById(R.id.tv_zhuangtai2);
        tv_zhuangtai3 = (TextView) findViewById(R.id.tv_zhuangtai3);

        tv_xuyao1 = (TextView) findViewById(R.id.tv_xuyao1);
        tv_xuyao2 = (TextView) findViewById(R.id.tv_xuyao2);
        tv_xuyao3 = (TextView) findViewById(R.id.tv_xuyao3);
        tv_xuyao4 = (TextView) findViewById(R.id.tv_xuyao4);
        tv_xuyao5 = (TextView) findViewById(R.id.tv_xuyao5);

        btn_faduanxin = (Button) findViewById(R.id.btn_faduanxin);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sosback:
                Intent intent = new Intent(SoSActivity.this, RunActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_huanjin1:
                if (tv_huanjin1.isSelected()) {
                    huanjin = "";
                    tv_huanjin1.setSelected(false);
                    tv_huanjin2.setClickable(true);
                    tv_huanjin3.setClickable(true);
                    tv_huanjin4.setClickable(true);
                    tv_huanjin5.setClickable(true);
                }else {
                    tv_huanjin1.setSelected(true);
                    tv_huanjin2.setClickable(false);
                    tv_huanjin3.setClickable(false);
                    tv_huanjin4.setClickable(false);
                    tv_huanjin5.setClickable(false);
                    huanjin = "车中";
                }
                break;
            case R.id.tv_huanjin2:
                if (tv_huanjin2.isSelected()) {
                    huanjin = "";
                    tv_huanjin2.setSelected(false);
                    tv_huanjin1.setClickable(true);
                    tv_huanjin3.setClickable(true);
                    tv_huanjin4.setClickable(true);
                    tv_huanjin5.setClickable(true);
                } else {
                    tv_huanjin2.setSelected(true);
                    tv_huanjin1.setClickable(false);
                    tv_huanjin3.setClickable(false);
                    tv_huanjin4.setClickable(false);
                    tv_huanjin5.setClickable(false);
                    huanjin = "室内";
                }
                break;
            case R.id.tv_huanjin3:
                if (tv_huanjin3.isSelected()) {
                    huanjin = "";
                    tv_huanjin3.setSelected(false);
                    tv_huanjin1.setClickable(true);
                    tv_huanjin2.setClickable(true);
                    tv_huanjin4.setClickable(true);
                    tv_huanjin5.setClickable(true);

                } else {
                    tv_huanjin3.setSelected(true);
                    tv_huanjin1.setClickable(false);
                    tv_huanjin2.setClickable(false);
                    tv_huanjin4.setClickable(false);
                    tv_huanjin5.setClickable(false);
                    huanjin = "街道";
                }
                break;
            case R.id.tv_huanjin4:
                if (tv_huanjin4.isSelected()) {
                    huanjin = "";
                    tv_huanjin4.setSelected(false);
                    tv_huanjin1.setClickable(true);
                    tv_huanjin3.setClickable(true);
                    tv_huanjin2.setClickable(true);
                    tv_huanjin5.setClickable(true);
                } else {
                    tv_huanjin4.setSelected(true);
                    tv_huanjin1.setClickable(false);
                    tv_huanjin3.setClickable(false);
                    tv_huanjin2.setClickable(false);
                    tv_huanjin5.setClickable(false);
                    huanjin = "荒芜的野外";
                }
                break;

            case R.id.tv_huanjin5:
                if (tv_huanjin5.isSelected()) {
                    huanjin = "";
                    tv_huanjin5.setSelected(false);
                    tv_huanjin1.setClickable(true);
                    tv_huanjin3.setClickable(true);
                    tv_huanjin2.setClickable(true);
                    tv_huanjin4.setClickable(true);
                }else {
                    tv_huanjin5.setSelected(true);
                    tv_huanjin1.setClickable(false);
                    tv_huanjin3.setClickable(false);
                    tv_huanjin2.setClickable(false);
                    tv_huanjin4.setClickable(false);
                    huanjin = "乘坐网约车";
                }
                break;

            case R.id.tv_zhuangtai1:
                if (tv_zhuangtai1.isSelected()) {
                    zhuangtai = "";
                    tv_zhuangtai1.setSelected(false);
                    tv_zhuangtai2.setClickable(true);
                    tv_zhuangtai3.setClickable(true);
                }else {
                    tv_zhuangtai1.setSelected(true);
                    tv_zhuangtai2.setClickable(false);
                    tv_zhuangtai3.setClickable(false);
                    zhuangtai = "被挟持";
                }
                break;
            case R.id.tv_zhuangtai2:
                if (tv_zhuangtai2.isSelected()) {
                    zhuangtai = "";
                    tv_zhuangtai2.setSelected(false);
                    tv_zhuangtai1.setClickable(true);
                    tv_zhuangtai3.setClickable(true);
                }else {
                    tv_zhuangtai2.setSelected(true);
                    tv_zhuangtai1.setClickable(false);
                    tv_zhuangtai3.setClickable(false);
                    zhuangtai = "被言语恐吓";
                }
                break;
            case R.id.tv_zhuangtai3:
                if (tv_zhuangtai3.isSelected()) {
                    zhuangtai = "";
                    tv_zhuangtai3.setSelected(false);
                    tv_zhuangtai2.setClickable(true);
                    tv_zhuangtai1.setClickable(true);
                }else {
                    tv_zhuangtai3.setSelected(true);
                    tv_zhuangtai2.setClickable(false);
                    tv_zhuangtai1.setClickable(false);
                    zhuangtai = "被暴力攻击";
                }
                break;

            case  R.id.tv_xuyao1:
                if (tv_xuyao1.isSelected()) {
                    xuyao1 = "";
                    tv_xuyao1.setSelected(false);
                    tv_xuyao2.setClickable(true);
                }else {
                    tv_xuyao1.setSelected(true);
                    tv_xuyao2.setClickable(false);
                    xuyao1 = "立刻报警";
                }
                break;
            case  R.id.tv_xuyao2:
                if (tv_xuyao2.isSelected()) {
                    xuyao1 = "";
                    tv_xuyao2.setSelected(false);
                    tv_xuyao1.setClickable(true);
                }else {
                    tv_xuyao2.setSelected(true);
                    tv_xuyao1.setClickable(false);
                    xuyao1 = "不用报警";
                }
                break;
            case  R.id.tv_xuyao3:
                if (tv_xuyao3.isSelected()) {
                    xuyao2 = "";
                    tv_xuyao3.setSelected(false);
                    tv_xuyao4.setClickable(true);
                }else {
                    tv_xuyao3.setSelected(true);
                    tv_xuyao4.setClickable(false);
                    xuyao2 = "立刻联系他(她)";
                }
                break;

            case R.id.tv_xuyao4:
                if (tv_xuyao4.isSelected()) {
                    xuyao2 = "";
                    tv_xuyao4.setSelected(false);
                    tv_xuyao3.setClickable(true);
                }else {
                    tv_xuyao4.setSelected(true);
                    tv_xuyao3.setClickable(true);
                    xuyao2= "无需直接联系他(她)";
                }
                break;
            case R.id.tv_xuyao5:
                if (tv_xuyao5.isSelected()) {
                    xuyao3 = "";
                    tv_xuyao5.setSelected(false);
                }else {
                    tv_xuyao5.setSelected(true);
                    xuyao3 = "跟据短信中发送的定位寻找他(她)。";
                }
                break;


            case R.id.btn_faduanxin:

                SharedPreferences preferences = getSharedPreferences("lxr", MODE_PRIVATE);
                String lxrname = preferences.getString("lxrname", "");
                if (lxrname != null && !lxrname.isEmpty()){
                    fasongjinji();

                }else {
                    Toast.makeText(SoSActivity.this, "请在设置中添加紧急联系人", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    //给紧急联系人发送信息
    private void fasongjinji() {
        SharedPreferences pref = getSharedPreferences("lxr", MODE_PRIVATE);
        String ln = pref.getString("lxrname", "");
        yourname = pref.getString("yourname", "");
        final String lnum = pref.getString("lxrnumber", "");


        alertDialog1 = new AlertDialog.Builder(SoSActivity.this).create();

        alertDialog1.show();
        Window window = alertDialog1.getWindow();  //获取Window
        window.setContentView(R.layout.alertdialog);
        TextView tv_dailogtitle = (TextView) window.findViewById(R.id.tv_dailogtitle);
        tv_dailogtitle.setText("SOS-发送至紧急联系人");
        TextView tv_dialogmessage = (TextView) window.findViewById(R.id.tv_dialogmessage);
        tv_dialogmessage.setText("  点击“发送”即给联系人 " + ln +"(" + lnum + ")"+" 发送紧急短信。(注:请勿在非危急或非必要的条件下使用此功能，以免对您产生不必要的损失)。"
                + "\n \n 联系人-"  + ln + "将收到以下内容的短信。"
                + "\n \n   【此短信为"+ yourname + "在危急情况下通过软件给您发送的紧急短信，请您在收到短信后立即与他(她)进行联系、或跟据短信所提供的信息采取其他必要措施,在与 " + yourname + " 取得联系并确认其安全之前" +
                "请对其动态及状况保持关注。(注：下面的信息均为" + yourname + "发送短信时给您提供的信息)"
                + "\n \n"+ yourname +" 所处的位置："  +  tv_cityloc.getText()
                + "\n"+ yourname +"所处的环境："  + huanjin
                + "\n"+yourname+"所处的状态： " + zhuangtai
                + "\n"+yourname+"需要您提供的帮助: " + xuyao1+ "," + xuyao2  + ","+xuyao3
                + "】" );

        duanxin1 =  " 此短信为"+ yourname + "在危急情况下通过手机软件给您发送的紧急短信，请您在收到短信后立即与他(她)进行联系、或跟据短信所提供的信息采取其他必要措施，在与 " + yourname + " 取得联系并确认其安全之前 " +
                "请对其动态及状况持续保持关注。(注：下面的信息均为" + yourname +"发送短信时给您提供的信息)"
                + "\n "+ yourname +"所处位置：" + tv_cityloc.getText()
                + "\n " + yourname +"所处的环境：" + huanjin
                + "\n " + yourname +"所处的状态：" + zhuangtai
                + "\n " + yourname +"需要您提供的帮助:" + xuyao1 + "," + xuyao2  + ","+xuyao3;

        TextView tv_alcancel = (TextView) window.findViewById(R.id.tv_alcancel);
        tv_alcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog1.dismiss();
            }
        });

        TextView tv_alok = (TextView) window.findViewById(R.id.tv_alok);
        tv_alok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (duanxin1 != null) {
                    SmsManager sms =  SmsManager.getDefault();

                    Intent sentintent = new Intent(SENT_SMS_ACTION);
                    PendingIntent sentPI = PendingIntent.getBroadcast(SoSActivity.this, 0, sentintent, 0);

                    Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
                    PendingIntent deliverPI = PendingIntent.getBroadcast(SoSActivity.this, 0, deliverIntent, 0);

                    if (duanxin1.length() > 70) {
                        ArrayList<String> msgs = sms.divideMessage(duanxin1);
                        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

                        for (int i = 0 ; i < msgs.size(); i++) {
                            sentIntents.add(sentPI);
                        }
                        sms.sendMultipartTextMessage(lnum, null, msgs, sentIntents, null);
                    }else {
                        sms.sendTextMessage(lnum, null, duanxin1,sentPI, deliverPI );

                    }
                }
                Toast.makeText(SoSActivity.this, "紧急联系人短信已在后台发送", Toast.LENGTH_SHORT).show();
                alertDialog1.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();   //在活动销毁的时候调用LocationClient 的 stop() 方法来停止定位，不然会不断刷新 耗电

    }
}

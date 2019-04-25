package com.yuyang.baiduguiji.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HeWeather {

    public String status;

    //基础类
    public Basic basic;

     //now天气类
    public  Now now;

    //指数类
    public Suggestion suggestion;





    /**
     * {
     "HeWeather":[
     {
     "basic":Object{...},
     "update":Object{...},
     "status":"ok",
     "now":Object{...},
     "daily_forecast":Array[3],
     "aqi":Object{...},
     "suggestion":Object{...}
     "hourly_forecast":Array[8],
     }
     ]
     }
     */

}

package com.yuyang.baiduguiji.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    //城市名称
    @SerializedName("city")
    public String cityName;

    //城市id
    @SerializedName("id")
    public String weatherId;

    //更新天气时间
    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;
    }

    /**
     *  "basic":{
     "city":"都昌",
     "cnty":"中国",
     "id":"CN101240210",
     "lat":"29.27510452",
     "lon":"116.20511627",
     "update":{
     "loc":"2018-07-15 14:48",
     "utc":"2018-07-15 06:48"
     }
     },
     */
}

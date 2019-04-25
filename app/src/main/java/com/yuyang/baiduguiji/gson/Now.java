package com.yuyang.baiduguiji.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    //天气状况
    @SerializedName("cond")
    public Cond cond;

    public class Cond{
        public String code;
        public String txt;
    }

    public String tmp;

  /*  //体感温度
    public String fl;

    //湿度
    public String hum;

    //降水量
    public String pcpn;




    //能见度
    public String vis;

    //大气压
    public String pres;

    //风
    public Wind wind;
    public class Wind{
        //风向
        public String dir;

        //风级
        public String sc;

        //风速
        public String spd;


    }*/

    /**
     * "now":{
     "cond":{
     "code":"100",
     "txt":"晴"
     },
     "fl":"36",
     "hum":"61",
     "pcpn":"0.0",
     "pres":"1007",
     "tmp":"33",
     "vis":"24",
     "wind":{
     "deg":"215",
     "dir":"西南风",
     "sc":"2",
     "spd":"9"
     }
     },
     */
}

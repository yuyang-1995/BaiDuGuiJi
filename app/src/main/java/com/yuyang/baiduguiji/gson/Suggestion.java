package com.yuyang.baiduguiji.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

  /*  //舒适度指数
    @SerializedName("comf")
    public  Comfort comfort;

    public class Comfort{

        public String txt;

        public String brf;

    }

    //洗车指数
    @SerializedName("cw")
    public  CarWash carWash;

    public class  CarWash{
        public String txt;

        public String brf;
    }

    //紫外线指数
    @SerializedName("uv")
    public  Fushe fushe;

    public class Fushe{
        public String txt;

        public String brf;

    }*/


    //运动指数
    public Sport sport;

    public class Sport{

        public String txt;

        public String brf;
    }

   /* //旅游指数
    public Trav trav;

    public class Trav{
        public String txt;

        public String brf;

    }

    //穿衣指数
    public  Drsg drsg;

    public class  Drsg{

        public String txt;

        public String brf;
    }*/


     /**
     *  "suggestion":{
     "air":{
     "brf":"中",
     "txt":"气象条件对空气污染物稀释、扩散和清除无明显影响，易感人群应适当减少室外活动时间。"
     },
     "comf":{
     "brf":"很不舒适",
     "txt":"白天天气晴好，但烈日炎炎会使您会感到很热，很不舒适。"
     },
     "cw":{
     "brf":"较适宜",
     "txt":"较适宜洗车，未来一天无雨，风力较小，擦洗一新的汽车至少能保持一天。"
     },
     "drsg":{
     "brf":"炎热",
     "txt":"天气炎热，建议着短衫、短裙、短裤、薄型T恤衫等清凉夏季服装。"
     },
     "flu":{
     "brf":"少发",
     "txt":"各项气象条件适宜，发生感冒机率较低。但请避免长期处于空调房间中，以防感冒。"
     },
     "sport":{
     "brf":"较不宜",
     "txt":"天气较好，但炎热，请注意适当减少运动时间并降低运动强度，户外运动请注意防晒。"
     },
     "trav":{
     "brf":"一般",
     "txt":"天气较好，同时又有微风伴您一路同行，但是比较热，外出旅游请注意防晒，并注意防暑降温。"
     },
     "uv":{
     "brf":"很强",
     "txt":"紫外线辐射极强，建议涂擦SPF20以上、PA++的防晒护肤品，尽量避免暴露于日光下。"
     }
     }
     */
}

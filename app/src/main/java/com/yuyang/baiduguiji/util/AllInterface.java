package com.yuyang.baiduguiji.util;



   public class AllInterface {

       //菜单滑动
    public  interface OnMenuSlideListener{
        void onMenuSlide(float offset);
    }

    //解锁
    public  interface IUnlock{
        void onUnlock();
    }

    //更新定位
    public  interface IUpdateLocation{
        void updateLocation(String totalTime, String totalDistance);
        void endLocation();
    }
}

package com.yuyang.baiduguiji.util;

import com.baidu.mapapi.model.LatLng;



  //Location Bean
  //保存当前坐标和地址
  public class LocationManager {

    LatLng currentLL;
    String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getCurrentLL() {
        return currentLL;
    }

    public void setCurrentLL(LatLng currentLL) {
        this.currentLL = currentLL;
    }

     //懒汉式单例
    public static LocationManager getInstance() {
        return SingletonFactory.singletonInstance;
    }

    private static class SingletonFactory {
        private static LocationManager singletonInstance = new LocationManager();
    }
}

package com.yuyang.baiduguiji.bean;

import android.os.Parcel;
import android.os.Parcelable;

  //运动坐标类
public class RoutePoint implements Parcelable {

     //此类记录运动过程中的时间，经纬度，速度等信息
    public int id;
    public long time;
    public double routeLat, routeLng, speed;  //经纬度，速度

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRouteLat() {
        return routeLat;
    }

    public void setRouteLat(double routeLat) {
        this.routeLat = routeLat;
    }

    public double getRouteLng() {
        return routeLng;
    }

    public void setRouteLng(double routeLng) {
        this.routeLng = routeLng;
    }

    @Override
    public int describeContents() {  //内容接口描述，默认返回0就可以。
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {  //，将你的对象序列化为一个Parcel对象，即：将类的数据写入外部提供的Parcel中，
        // 打包需要传递的数据到Parcel容器保存，以便从 Parcel容器获取数据。
        parcel.writeDouble(routeLat);
        parcel.writeDouble(routeLng);
        parcel.writeDouble(speed);
        parcel.writeLong(time);
        parcel.writeInt(id);

    }

    public static final Parcelable.Creator<RoutePoint> CREATOR = new Creator<RoutePoint>() { //实例化静态内部对象CREATOR实现接口Parcelable.Creator
        @Override
        public RoutePoint createFromParcel(Parcel source) {
            RoutePoint routePoint = new RoutePoint();
            routePoint.id = source.readInt();
            routePoint.routeLat = source.readDouble();
            routePoint.routeLng = source.readDouble();
            routePoint.speed = source.readDouble();
            routePoint.time = source.readLong();
            return routePoint;
        }

        @Override
        public RoutePoint[] newArray(int size) {
// TODO Auto-generated method stub
            return new RoutePoint[size];
        }
    };
}


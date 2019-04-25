package com.yuyang.baiduguiji.bean;

import android.os.Parcel;
import android.os.Parcelable;

import cn.bmob.v3.BmobObject;


//运动记录
public class RouteRecord extends BmobObject implements Parcelable {

    public String cycle_date;  //日期
    public String cycle_time;  //运动时长
    public String cycle_distance; //距离
    public String cycle_points;  //坐标点
    public String cycle_step;      //运动步数

      public String getCycle_step() {
          return cycle_step;
      }

      public void setCycle_step(String cycle_step) {
          this.cycle_step = cycle_step;
      }

      public RouteRecord() {
    }

    public String getCycle_date() {
        return cycle_date;
    }

    public void setCycle_date(String cycle_date) {
        this.cycle_date = cycle_date;
    }

    public String getCycle_time() {
        return cycle_time;
    }

    public void setCycle_time(String cycle_time) {
        this.cycle_time = cycle_time;
    }

    public String getCycle_distance() {
        return cycle_distance;
    }

    public void setCycle_distance(String cycle_distance) {
        this.cycle_distance = cycle_distance;
    }

    public String getCycle_points() {
        return cycle_points;
    }

    public void setCycle_points(String cycle_points) {
        this.cycle_points = cycle_points;
    }


    public static final Parcelable.Creator<RouteRecord> CREATOR = new Creator<RouteRecord>() {
        @Override
        public RouteRecord createFromParcel(Parcel source) {

            return new RouteRecord(source);
        }

        @Override
        public RouteRecord[] newArray(int size) {
            return new RouteRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cycle_date);
        dest.writeString(cycle_time);
        dest.writeString(cycle_distance);
        dest.writeString(cycle_points);
        dest.writeString(cycle_step);
   }

    private RouteRecord(Parcel source) {
        cycle_date = source.readString();
        cycle_time = source.readString();
        cycle_distance = source.readString();
        cycle_points = source.readString();
        cycle_step = source.readString();
    }
}

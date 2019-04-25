package com.yuyang.baiduguiji.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

//路程信息表，记录 运动时间、距离、日期、 点
  public class RouteDBHelper extends SQLiteOpenHelper {

      Context mContext;

       //继承SQLiteOpenHelper类创建“历史运动”SQLlite数据库。
    public RouteDBHelper(Context context) {
        super(context, "route_history.db", null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
  //创建一个SQLite数据库， 记录运动日期， 时间，距离， 地理位置以及运动步数， 数据类型都为文本类型
        //表名为cycle_route
        db.execSQL(  "CREATE TABLE IF NOT EXISTS  cycle_route (route_id integer primary key autoincrement ," +
                "cycle_date text not null ," +
                "cycle_time text not null ," +
                "cycle_distance text not null ," +
                "cycle_step text not null," +
                 "cycle_points text not null )"   );
        Toast.makeText(mContext, "创建route_history.db成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

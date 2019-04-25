package com.yuyang.baiduguiji.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.bean.RouteRecord;

import java.util.List;

  //适配器，
  public class MyRouteAdapter extends RecyclerView.Adapter<MyRouteAdapter.MyViewHolder> {

    public Context context;
    int selectPosition = 0;
    OnItemClickListener listener;  //给适配器定义一个监听器， 适配器是给View注入数据的组件
    List<RouteRecord> list;

     //构造函数参数， Context 和RouteRecord集合
    public MyRouteAdapter(Context context, List<RouteRecord> list) {
        this.context = context;
        this.list = list;
    }

    //
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_route_item, null); //将列表项注入，列表含有4个属性需要显示
        MyViewHolder holder = new MyViewHolder(view); //将数据项的view注入创建ViewHolder

        view.setOnClickListener(new View.OnClickListener() {   //给列表项注册监听器
            @Override
            public void onClick(View view) {
             int position = (int) view.getTag();   //获取列表项的id
                if (listener != null) {   //当借口不为空
                    listener.onItemClick(view, position);  //执行方法
                }
            }
        });
        return holder;
    }

    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.itemView.setTag(position);
        RouteRecord routeRecord=list.get(position);
        holder.bike_time.setText(routeRecord.getCycle_time());
        holder.bike_distance.setText(routeRecord.getCycle_distance());
        holder.bike_date.setText(routeRecord.getCycle_date());
        holder.bike_step.setText(routeRecord.getCycle_step());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //内部类, ViewHolder 中有4个属性需要显示
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView bike_time,bike_distance,bike_date,bike_step;

        public MyViewHolder(View view) {
            super(view);
            bike_time = (TextView) view.findViewById(R.id.bike_time);
            bike_distance = (TextView) view.findViewById(R.id.bike_distance);
            bike_step = (TextView) view.findViewById(R.id.bike_step);
            bike_date = (TextView) view.findViewById(R.id.bike_date);
        }
    }

    public interface OnItemClickListener {  //监听器
        public void onItemClick(View v, int position);  //监听器实现方法，具体由Activity实现
    }

    public void setOnClickListener(OnItemClickListener listener) {  //绑定监听器方法
        this.listener = listener;
    }

}

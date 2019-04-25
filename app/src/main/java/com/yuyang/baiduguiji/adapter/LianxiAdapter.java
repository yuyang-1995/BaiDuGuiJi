package com.yuyang.baiduguiji.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yuyang.baiduguiji.R;
import com.yuyang.baiduguiji.bean.Lianxiren;

import java.util.List;

public class LianxiAdapter extends ArrayAdapter<Lianxiren> {
    private int reaourceId;


    public LianxiAdapter(Context context, int textViewResourceId, List<Lianxiren> objects){
        super(context, textViewResourceId, objects);
        reaourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Lianxiren lianxiren = getItem(position);  //获取当前实例
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(reaourceId, parent, false);
             viewHolder = new ViewHolder();
             viewHolder.lxname = (TextView) view.findViewById(R.id.tv_name);
             viewHolder.lxnumber = (TextView) view.findViewById(R.id.tv_number);
             view.setTag(viewHolder);

        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.lxnumber.setText(lianxiren.getNumber());
        viewHolder.lxname.setText(lianxiren.getName());
         return view;
    }

     class ViewHolder {
        TextView lxname;
        TextView lxnumber;

    }
}

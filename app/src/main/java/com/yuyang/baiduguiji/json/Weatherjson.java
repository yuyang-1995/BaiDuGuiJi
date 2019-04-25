package com.yuyang.baiduguiji.json;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yuyang.baiduguiji.gson.HeWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Weatherjson {

    private static final String TAG = "WeatherJson";
    public static HeWeather getWeatherResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
                String weateherContent = jsonArray.getJSONObject(0).toString();
                return new Gson().fromJson(weateherContent, HeWeather.class);  //GSON
            } catch (JSONException e) {
                e.printStackTrace();
             //   LogUtil.d(TAG, "getWeatherResponse: ");
            }
        }
        return null;
    }
}

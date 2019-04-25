package com.yuyang.baiduguiji.bean;

import java.util.List;

/**
 * Author: yuyang
 * Date:2018/12/17 14:39
 */
public class FoodType {

    private String food_type;
    private List<FoodMessage> food_list;

    public String getFood_type() {
        return food_type;
    }

    public void setFood_type(String food_type) {
        this.food_type = food_type;
    }

    public List<FoodMessage> getFood_list() {
        return food_list;
    }

    public void setFood_list(List<FoodMessage> food_list) {
        this.food_list = food_list;
    }


}

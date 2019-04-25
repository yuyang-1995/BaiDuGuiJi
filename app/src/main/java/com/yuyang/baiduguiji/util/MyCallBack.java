package com.yuyang.baiduguiji.util;

import java.io.IOException;

public interface MyCallBack {

    void onFailure(IOException e);

    void onResponse(String response) throws IOException;
}

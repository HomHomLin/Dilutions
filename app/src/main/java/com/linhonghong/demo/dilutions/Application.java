package com.linhonghong.demo.dilutions;

import com.linhonghong.dilutions.Dilutions;

public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        Dilutions.init(this);
        //添加协议头
        Dilutions.create().addScheme("dilutions2");
    }
}

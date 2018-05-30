package com.linhonghong.demo.dilutions;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.linhonghong.dilutions.DilutionsValue;
import com.linhonghong.dilutions.annotations.MethodExtra;
import com.linhonghong.dilutions.annotations.MethodParam;
import com.linhonghong.dilutions.annotations.MethodProtocol;

import java.util.HashMap;

/**
 * 演示方法协议调用
 * Created by Linhh on 2017/6/26.
 */

public class Method {

    @MethodProtocol("/obj")
    public void obj(@MethodParam("tt") TestObj k, @MethodParam("mycallback") View.OnClickListener object){
        Log.d("testmethodobj", "obj is called:" + object);
        object.onClick(null);
    }

    @MethodProtocol("/testmap")
    public void obj(@MethodParam("tt") HashMap<String, Object> k){
        Log.d("testmethodobj", "obj is called.");
    }

    /**
     * 通过协议/finish可以直接跑到这里,无参的
     */
    @MethodProtocol("/finish")
    public void finish(){
        Log.d("finish","this Activity is finishing.");
    }

    @MethodProtocol("/doubles")
    public double dilutionsDouble(@MethodParam("doubles") double s){
        Log.d("dilutionsDouble","dilutionsDouble is call." + s);
        return 10.3;
    }

    /**
     * 通过"/circles/group"协议
     * @param l 读取协议的groupID参数
     * @param b
     * @param a
     * @param t 读取协议的test参数
     */
    @MethodProtocol("/circles/group")
    public void test1(@MethodParam("groupID") int l,
                      Bundle b,
                      int a,
                      @MethodParam("test") String t){
        Log.i("dilutions_test","n:" + l + ",t:" + t);
    }

    /**
     * 通过协议twapro2
     * @param a
     */
    @MethodProtocol("twapro2")
    public void test1(int a){
        Log.d("test","test1 had called.");
    }

    /**
     * 通过协议twapro3
     * @param t 参数tree
     */
    @MethodProtocol("twapro3")
    public void test2(@MethodParam("tree") String t, @MethodExtra(DilutionsValue.DILUTIONS_METHOD_EXTRA) Object object){
        Log.d("test","twapro3 had called.");
    }
}

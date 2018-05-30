package com.linhonghong.dilutions.data;


import java.lang.reflect.Method;

/**
 * Created by Linhh on 16/12/23.
 */

public class DilutionsMethodData {
    public Object obj;
    public Method method;
    public DilutionsMethodData(Object obj, Method method){
        this.obj = obj;
        this.method = method;
    }
}

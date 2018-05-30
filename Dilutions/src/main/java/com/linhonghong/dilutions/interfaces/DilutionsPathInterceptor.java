package com.linhonghong.dilutions.interfaces;

/**
 * Created by Linhh on 2017/8/11.
 */

public abstract class DilutionsPathInterceptor implements DilutionsInterceptor{

    public final static int LEVEL_NORMAL = 0;

    public int level(){
        return LEVEL_NORMAL;
    }
}

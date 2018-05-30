package com.linhonghong.dilutions.interfaces;

import com.linhonghong.dilutions.data.DilutionsData;

/**
 * 数据拦截
 * Created by Linhh on 16/12/16.
 */

public interface DilutionsInterceptor<T> {
    public boolean interceptor(DilutionsData<T> data);
}

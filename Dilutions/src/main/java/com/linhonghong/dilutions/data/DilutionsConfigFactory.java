package com.linhonghong.dilutions.data;

import com.linhonghong.dilutions.interfaces.DilutionsCallBack;
import com.linhonghong.dilutions.interfaces.DilutionsInterceptor;

/**
 * Created by Linhh on 16/12/16.
 */

public class DilutionsConfigFactory {
    public static <T> DilutionsConfig.Builder newBuilder(DilutionsCallBack<T> dilutionsCallBack, DilutionsInterceptor<T> interceptor, T what) {
        return new DilutionsConfig.Builder<T>()
                .setDilutionsCallBack(dilutionsCallBack)
                .setDilutionsInterceptor(interceptor)
                .setWhat(what);
    }
}

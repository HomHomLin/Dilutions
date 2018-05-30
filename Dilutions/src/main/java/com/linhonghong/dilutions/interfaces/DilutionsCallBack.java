package com.linhonghong.dilutions.interfaces;

import com.linhonghong.dilutions.data.DilutionsData;

/**
 * 回调处理器
 * Created by Linhh on 16/12/16.
 */

public interface DilutionsCallBack<T> {
    public void onDilutions(DilutionsData<T> data);
}

package com.linhonghong.dilutions.data;

/**
 * Created by Linhh on 2017/7/10.
 */

public interface DilutionsGlobalListener {
    public boolean onCheckUri(String uri);

    public boolean onUnSupportUri(String uri);

    public boolean onIntercept(DilutionsData data);

    public boolean onCallBack(DilutionsData data);
}

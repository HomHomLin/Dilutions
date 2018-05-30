package com.linhonghong.dilutions.data;

import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;

/**
 * 数据包裹处理器
 * Created by Linhh on 16/12/16.
 */

public class DilutionsData<T> {
    private Intent mIntent;
    private T what;
    private Uri mUri;
    private Object mResult;
    private ArrayList<String> mList;
    public Intent getIntent(){
        return mIntent;
    }

    public void setList(ArrayList<String> list){
        mList = list;
    }

    public ArrayList<String> getList(){
        return mList;
    }

    public T getWhat(){
        return what;
    }

    public void setIntent(Intent intent){
        mIntent = intent;
    }

    public void setUri(Uri uri){
        mUri = uri;
    }

    public void setResult(Object result){
        mResult = result;
    }

    public Object getResult(){
        return mResult;
    }

    public Uri getUri(){
        return mUri;
    }

    public void setWhat(T what){
        this.what = what;
    }
}

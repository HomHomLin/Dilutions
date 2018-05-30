package com.linhonghong.dilutions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.alibaba.fastjson.JSONObject;
import com.linhonghong.dilutions.data.DilutionsData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Linhh on 16/11/30.
 */

public class DilutionsBuilder {
    Intent extrasIntent = new Intent();
    Map<String, Extra> extras = new HashMap<>();
    JSONObject jsonObject = new JSONObject();
    JSONObject paramsJson = new JSONObject();
    Uri uri;

    private DilutionsData data;

    private int mProtocolFrom;//来自哪里的跳转?

    private String mPath;
    private Context mContext;
    private ProtocolClazzORMethod protocolClazzORMethod;

    private Object what;

    DilutionsBuilder(Context context){
        mContext = context;
    }

    class Extra{
        public Object value;
        public Class<?> type;
    }

    public String getPath(){
        return mPath;
    }

    public void setFrom(int from){
        mProtocolFrom = from;
    }

    public void setUri(Uri uri){
        this.uri = uri;
    }

    public int getFrom(){
        return mProtocolFrom;
    }
    /**
     * 添加post参数
     * @param name
     * @param value
     */
    public void addParams(String name, Object value, Class<?> type) throws Exception{
        Extra extra = new Extra();
        extra.type = type;
        extra.value = value;
        jsonObject.put(name, value);
        extras.put(name, extra);
//        extras.put(name,value);
    }

    public void setClazz(int protocolType, Context context, String path,  ProtocolClazzORMethod clazz){
        if(protocolType == DilutionsValue.PROTOCOL_JUMP){
            extrasIntent.setClass(mContext, clazz.clazz);
            extrasIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        protocolClazzORMethod = clazz;
        mPath = path;
    }

    public void setClazz(int protocolType, String path, ProtocolClazzORMethod clazz){
        setClazz(protocolType, mContext, path, clazz);
    }

    public void setWhat(Object what){
        this.what = what;
    }

    public DilutionsData getDilutionsData(){
        if(data == null){
            data = new DilutionsData();
        }
        data.setIntent(getIntent());
        data.setWhat(what);
        data.setUri(uri);
        data.setList(protocolClazzORMethod.mParamsList);
        return data;
    }

    public Intent getIntent(){
//        intent.putExtra(URI_CALL_PATH, path);
        paramsJson.put(DilutionsValue.VAL_PARAMS, jsonObject);
        putExtra(extrasIntent);
        extrasIntent.putExtra(DilutionsInstrument.URI_CALL_CLASS, protocolClazzORMethod.clazz);
        extrasIntent.putExtra(DilutionsInstrument.URI_CALL_PATH, mPath);
        extrasIntent.putExtra(DilutionsInstrument.URI_CALL_PARAM,paramsJson.toString());
        extrasIntent.putExtra(DilutionsInstrument.URI_FROM, mProtocolFrom);
        //可能来自代理跳转的,不存在uri
        extrasIntent.putExtra(DilutionsInstrument.URI_CALL_ALL, uri == null ? "from clazz protocol" : uri.toString());
        return extrasIntent;
    }

    public Intent putExtra(Intent intent){
        for (Map.Entry<String, Extra> entry : extras.entrySet()) {
            String key = entry.getKey();
            Extra extra = entry.getValue();
            Object obj = extra.value;
            Class<?> type = extra.type;
//            checkMap.put("int", Integer.class);
//            checkMap.put("String", String.class);
//            checkMap.put("long", Long.class);
//            checkMap.put("double", Double.class);
//            checkMap.put("boolean", Boolean.class);
//            checkMap.put("float",Float.class);
            if(type == String.class){
                String value = (String)obj;
                intent.putExtra(key, value);
            }else if(type == Integer.class || type == int.class){
                Integer value = (Integer)obj;
                intent.putExtra(key,value);
            }else if(type == Long.class || type == long.class){
                Long value = (Long)obj;
                intent.putExtra(key,value);
            }else if(type == Double.class || type == double.class){
                Double value = (Double)obj;
                intent.putExtra(key,value);
            }else if(type == Boolean.class || type == boolean.class){
                Boolean value = (Boolean)obj;
                intent.putExtra(key,value);
            }else if(type == Float.class || type == float.class){
                Float value = (Float)obj;
                intent.putExtra(key,value);
            }
            else{
                try {
                    intent.putExtra(key, (Serializable) obj);
                }catch (Exception e){
                    //不支持该类型
                    e.printStackTrace();
                }
            }
        }
        return intent;
    }
}

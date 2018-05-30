package com.linhonghong.dilutions.utils;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;

import com.linhonghong.dilutions.DilutionsInstrument;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Linhh on 16/11/30.
 */

public class DilutionsUtil {

    public static String ENCODING = "UTF-8";

    public static Object getParams(String type){
        if("boolean".equals(type)){
            return false;
        }
        if("char".equals(type)){
            return null;
        }
        if("short".equals(type)){
            return 0;
        }
        if("int".equals(type)){
            return 0;
        }
        if("float".equals(type)){
            return 0f;
        }
        if("double".equals(type)){
            return 0f;
        }
        if("long".equals(type)){
            return 0;
        }
        if("byte".equals(type)){
            return null;
        }
        return null;
    }

    public static Class<?> getParamsClass(String type) throws Exception{
        if("boolean".equals(type)){
            return boolean.class;
        }
        if("char".equals(type)){
            return char.class;
        }
        if("short".equals(type)){
            return short.class;
        }
        if("int".equals(type)){
            return int.class;
        }
        if("float".equals(type)){
            return float.class;
        }
        if("double".equals(type)){
            return double.class;
        }
        if("long".equals(type)){
            return long.class;
        }
        return Class.forName(type);
    }

    public static Object formatString(Object obj){
        if(obj == null){
            return "";
        }
        if(obj instanceof String){
            return String.valueOf(obj);
        }
        return obj;
    }

    /**
     * BASE64 解码，URL_SAFE
     *
     * @param input
     * @return
     */
    //TODO
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static String base64UrlDecode(String input) {
        input = input.replace("/", "_").replace("+", "-").replace("=", "");
        String result = null;
        byte[] b = Base64.decode(input, Base64.URL_SAFE);
        try {
            result = new String(b, ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isNull(String str) {
        try {
            if (str == null) {
                return true;
            } else if (str != null) {
                if (str.equals("") || str.equals("null") || str.equals("[]")) {
                    return true;
                } else return str.trim().equals("") || str.trim().equals("null");
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;

    }

    public static boolean isEqual(String src, String dest) {
        try {
            if (src == null)
                src = "";
            if (dest == null)
                dest = "";
            return src.equals(dest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 内部放path需要的数据
     */
    public static final String PARAMS_KEY = "params";

/*
    public static String ID = "id";
    public static String URL = "url";
    public static String COMMUNITY_CATEGORY_TAB = "community_category_tab";*/


    public static boolean isFromUri(Intent intent) {
        if (intent == null) {
            return false;
        }
        return isFromUri(intent.getExtras());
    }

    public static boolean isFromUri(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        String json = bundle.getString(DilutionsInstrument.URI_CALL_PARAM);
        if (isNull(json)) {
            return false;
        }
        return true;
    }

    public static String getValue(String name, Bundle bundle) {
        try {
            String json = bundle.getString(DilutionsInstrument.URI_CALL_PARAM);
            if (!isNull(json)) {
                JSONObject jsonObject = new JSONObject(json);
                String jsonValue = jsonObject.getString("params");
                if (!isNull(jsonValue)) {
                    JSONObject job = new JSONObject(jsonValue);
                    Object value = job.get(name);
                    if (value != null) {
                        if (value instanceof Integer) {
                            return (Integer) value + "";
                        }
                        return value.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getValue(String name, String defValue, Bundle bundle) {
        try {
            String json = bundle.getString(DilutionsInstrument.URI_CALL_PARAM);
            if (!isNull(json)) {
                JSONObject jsonObject = new JSONObject(json);
                String jsonValue = jsonObject.optString(PARAMS_KEY);
                if (!isNull(jsonValue)) {
                    JSONObject job = new JSONObject(jsonValue);
                    Object value = job.opt(name);
                    if (value != null) {
                        if (value instanceof Integer) {
                            return (Integer) value + "";
                        }
                        return value.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    public static String getParamByBundle(Bundle bundle) {
        try {
            String json = bundle.getString(DilutionsInstrument.URI_CALL_PARAM);
            if (!isNull(json)) {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.getString(PARAMS_KEY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 获取Intent参数值
     *
     * @param name；参数名字；UriParam 提供了很多通用的参数，可以直接用；
     * @param intent
     * @return
     */
    public static String getIntentParam(String name, Intent intent) {
        return getValue(name, "", intent.getExtras());
    }

    public static com.alibaba.fastjson.JSONObject getUriParamsWithString(String uri_s) throws Exception {
        Uri uri = Uri.parse(uri_s);
        String params = uri.getQueryParameter("params");
        params = DilutionsUtil.base64UrlDecode(params);
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(params);
        return jsonObject;
    }
}

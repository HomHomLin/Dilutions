package com.linhonghong.dilutions.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.alibaba.fastjson.JSONObject;
import com.linhonghong.dilutions.DilutionsValue;

import java.io.UnsupportedEncodingException;

/**
 * Created by Linhh on 2017/7/28.
 */

public class DilutionsUriBuilder {

    /**
     * 构造通用的Uri 协议
     * <p>
     * test linhonghong://dilutions/test
     *
     * @param path  如： group ，前面不要带"/"
     * @param param
     * @return
     */
    public static String buildUri(String scheme, String path, JSONObject param) {
        String json = "";
        if (param != null) {
            json = param.toString();
        }

        return buildUri(scheme, path, json);
    }

    /**********
     * 私有方法
     **********/

    public static String buildUri(String scheme, String path, String json) {
        String mHost = "";
        if(!isNull(path) && path.startsWith("/")){
            path = path.substring(1, path.length());
        }
        String mPath = path;
        if(!isNull(scheme) && scheme.endsWith("://")){
            scheme = scheme.substring(0, scheme.length() - 3);
        }
        String query = buildQuery(json);
        return scheme + ":" + "//" + mHost + "/" + mPath + "?" + query;
    }

    public static boolean isNull(String str) {
        try {
            if (str == null) {
                return true;
            } else if (str != null) {
                if (str.equals("") || str.equals("null") || str.equals("[]")) {
                    return true;
                } else if (str.trim().equals("") || str.trim().equals("null")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;

    }

    /**
     * 获取Query，需要进行 base64 编码
     *
     * @param jsonObject
     * @return
     */
    private static String buildQuery(JSONObject jsonObject) {
        if (jsonObject == null) {
            return "";
        }
        return buildQuery(jsonObject.toString());
    }

    private static String buildQuery(String json) {
        if (TextUtils.isEmpty(json)) {
            return "";
        }

        return DilutionsValue.VAL_PARAMS + "=" + base64UrlEncode(json);

    }

    /**
     * BASE64 编码，URL_SAFE
     *
     * @param input
     * @return
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static String base64UrlEncode(String input) {
        String ENCODING = "UTF-8";
        String result = null;
        byte[] b = Base64.encode(input.getBytes(), Base64.URL_SAFE);
        try {
            result = new String(b, ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

}

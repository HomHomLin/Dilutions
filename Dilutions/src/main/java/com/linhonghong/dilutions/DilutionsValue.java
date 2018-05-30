package com.linhonghong.dilutions;

/**
 * Created by Linhh on 16/9/5.
 */
public class DilutionsValue {
    public static final int DEFAULT_VALUE = -1;
    public static final String VAL_PARAMS = "params";

    public static final int PROTOCOL_DEFAULT = 0;
    public static final int PROTOCOL_JUMP = 1;
    public static final int PROTOCOL_METHOD = 2;

    public static final int DILUTIONS_NO = 0;//不来自dilutions
    public static final int DILUTIONS_PROXY = 1;//来自代理
    public static final int DILUTIONS_HTTP = 2;//来自WEBVIEW
    public static final String DILUTIONS_METHOD_EXTRA = "dilutions_method_params_extra";


}

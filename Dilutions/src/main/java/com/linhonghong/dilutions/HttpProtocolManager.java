package com.linhonghong.dilutions;

import android.net.Uri;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.linhonghong.dilutions.utils.DilutionsUtil;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Linhh on 16/12/1.
 */

public class HttpProtocolManager<T> implements DilutionsManager<T> {

    private final static String TAG = HttpProtocolManager.class.getSimpleName();

    //基础数据
    ArrayList<ParameterHanlder<?>> parameterHandlers;
    ProtocolClazzORMethod protocolClazzORMethod;//跳转目标
    DilutionsBuilder dilutionsBuilder;
    int protocolType;
    String methodName;

//    String[] args;

    HttpProtocolManager(Builder<T> builder) {
        this.protocolType = builder.protocolType;
        this.parameterHandlers = builder.parameterHandlers;
        this.protocolClazzORMethod = builder.protocolClazzORMethod;
        this.methodName = builder.protocolClazzORMethod.methodName;
        this.dilutionsBuilder = builder.dilutionsBuilder;
    }

    /**
     * 生效请求配置
     * @throws Exception
     */
    public void apply() throws Exception{
        for(int i = 0; i < parameterHandlers.size(); i ++){
            parameterHandlers.get(i).apply(dilutionsBuilder);
        }
    }

    public ProtocolClazzORMethod getProtocolClazzORMethod(){
        return protocolClazzORMethod;
    }

    @Override
    public int getProtocolType() {
        return protocolType;
    }

    public DilutionsBuilder getDilutionsBuilder(){
        return dilutionsBuilder;
    }

    static final class Builder<T> {

        final DilutionsInstrument instrument;

        ProtocolClazzORMethod protocolClazzORMethod;//跳转目标

        //参数
        ArrayList<ParameterHanlder<?>> parameterHandlers;

        DilutionsBuilder dilutionsBuilder;

        Uri uri;

        HashMap<String, Object> mExtraMap;

        int protocolType = DilutionsValue.PROTOCOL_DEFAULT;//协议类型,如若在解析过程中始终保持default状态就会报错

        public Builder(DilutionsInstrument instrument, Uri uri, HashMap<String, Object> map) {
            this.instrument = instrument;
            this.uri = uri;
            this.dilutionsBuilder = instrument.getDilutionsBuilder();
            this.mExtraMap = map;
        }

        /**
         * 解析方法注解
         * @param uri
         */
        private void parseUri(Uri uri) throws Exception{
            if(uri == null){
                throw new Exception("URI 协议为空, 解析失败");
            }

//            LogUtils.d(TAG, " Dilutions获取到http协议: " + uri.toString());

            String host = uri.getHost();
            String scheme = uri.getScheme();
            String path = uri.getPath();
            String query = uri.getQuery();

            if (scheme == null || scheme.trim().equals("")) {
                throw new Exception("scheme为空, 解析失败");
            }

            //合法性检查
//            if(!instrument.checkUriSafe(scheme, path)){
//                throw new Exception("协议不合法");
//            }

            //获得该URI的类型
            protocolType = instrument.getUriType(scheme, path);

            parserParams(protocolType, path, query);

            parseMethodPath(protocolType, path);

            if(mExtraMap != null){
                parseParameter(protocolType, path, mExtraMap);
            }
        }

        /**
         * 解析参数
         * @param path
         * @param query
         * @return
         */
        public void parserParams(int protocolType, String path, String query) throws Exception{
//            Map<String, String> queryPairs = new LinkedHashMap<>();
            if (DilutionsUtil.isNull(query)) {
//                throw new Exception("dilutions protocol query is null");
                return;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                if(DilutionsUtil.isEqual("params", key)) {
    //              String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
    //              byte[] decodeBytes = Base64.decode(value, Base64.DEFAULT);
                    String value = pair.substring(idx + 1);
                    value = DilutionsUtil.base64UrlDecode(value);
//                    queryPairs.put(key, value);
//                    queryPairs.get("params")

                    parseParameter(protocolType, path, value);
                }
                //如果有额外的参数请添加于此

            }
        }

        /**
         * 根据名字找到API
         * @param value
         * @param protocolType 协议类型
         */
        private void parseMethodPath(int protocolType, String value) throws Exception{

            protocolClazzORMethod = instrument.getClazz(protocolType, value);

            if(protocolClazzORMethod.clazz == null){
                //错误
                throw new Exception("clazz is null");
            }

            //设置dilutions的跳转类,因为intent会被重新创建,所以这里要复用设置
            dilutionsBuilder.setClazz(protocolType, value, protocolClazzORMethod);

        }

        public HttpProtocolManager build() throws Exception{
            //存储参数处理器
            parameterHandlers = new ArrayList<>();

            parseUri(uri);

            dilutionsBuilder.setFrom(DilutionsValue.DILUTIONS_HTTP);//代理跳转

            dilutionsBuilder.setUri(uri);

            return new HttpProtocolManager(this);
        }

        private void parseParameter(int protocolType, String path, HashMap<String, Object> value) throws Exception{
            JSONObject jsonObject = new JSONObject(value);//将参数转换为json
            //构造参数处理器
            instrument.createExtraParams(
                    parameterHandlers,
                    path,
                    jsonObject);
            //没有能识别的参数，如果出现这个说明出错了
        }

        private void parseParameter(int protocolType, String path, String value) throws Exception{
            JSONObject jsonObject = JSON.parseObject(value);//将参数转换为json
            //构造参数处理器
            instrument.createExtraParams(
                    parameterHandlers,
                    path,
                    jsonObject);
            //没有能识别的参数，如果出现这个说明出错了
        }
    }
}

/**
 * 协议处理器封装类
 */
class ProtocolClazzORMethod{
    public String mPath;
    public Class<?> clazz;
    public String methodName;
    public ArrayList<String> mParamsList;
    public Class<?>[] mClasses;
    public ProtocolClazzORMethod(Class<?> clazz, String methodName, String path, ArrayList<String> paramsList,Class<?>[] classes){
        this.clazz = clazz;
        this.methodName = methodName;
        this.mPath = path;
        this.mParamsList = paramsList;
        this.mClasses = classes;
    }
}

package com.linhonghong.dilutions;

import android.content.Context;
import android.net.Uri;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.linhonghong.dilutions.data.DilutionsConfig;
import com.linhonghong.dilutions.data.DilutionsConfigFactory;
import com.linhonghong.dilutions.data.DilutionsGlobalListener;
import com.linhonghong.dilutions.utils.Checker;
import com.linhonghong.dilutions.utils.DilutionsUtil;
import com.linhonghong.dilutions.interfaces.DilutionsCallBack;
import com.linhonghong.dilutions.interfaces.DilutionsInterceptor;
import com.linhonghong.dilutions.interfaces.DilutionsPathInterceptor;
import com.linhonghong.dilutions.utils.DilutionsUriBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 稀释项目框架
 * 提供模块间以及UI间的高性能解耦数据通信功能
 * 请不要随意修改该框架,谢谢合作
 * 如果有需要修改请先联系林宏弘,谢谢
 * Created by Linhh on 16/9/2.
 */
public class Dilutions {

    private final DilutionsInstrument dilutionsInstrument;
//    private static boolean mIsInit = false;

    private static Dilutions dilutions;

    /**
     * 初始化,需要在application内初始化
     * @param context
     */
    @Deprecated
    public static void init(Context context, String pathname){
        if(dilutions == null){
            List<String> path_list = new ArrayList<>();
            if(!DilutionsUtil.isNull(pathname)) {
                path_list.add(pathname);
            }

            dilutions = new Dilutions.Builder()
                    .pathName(path_list)
                    .context(context)
                    .build();
        }
    }

    public static void init(Context context, List<String> pathname){
        if(dilutions == null){
            dilutions = new Dilutions.Builder()
                    .pathName(pathname)
                    .context(context)
                    .build();
        }
    }

    public static void init(Context context){
        init(context, DilutionsInstrument.PATH_JUMP_FILE);
    }

    public static Dilutions create(){
        if(dilutions == null){
            //没有初始化成功,错误
            return null;
        }
        return dilutions;
    }

    Dilutions(Builder builder) {
        dilutionsInstrument = new DilutionsInstrument(builder.context, builder.pathName);
        try {
            //初始化
            dilutionsInstrument.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeDilutionsPathInterceptor(String path, DilutionsPathInterceptor interceptor){
        dilutionsInstrument.removeDilutionsPathInterceptor(path, interceptor);
    }

    public void setDilutionsPathInterceptor(String path ,DilutionsPathInterceptor dilutionsPathInterceptor) {
        dilutionsInstrument.setDilutionsPathInterceptor(path, dilutionsPathInterceptor);
    }

    @Deprecated
    public void setDilutionsGlobalListener(DilutionsGlobalListener dilutionsGlobalListener){
        dilutionsInstrument.setDilutionsGlobalListener(dilutionsGlobalListener);
    }

    /**
     * 处理HTtp协议
     * @param uri_string
     */
    public boolean formatProtocolService(final String uri_string){
        return formatProtocolService(uri_string, null);
    }

    /**
     * 自定义协议发起
     * @param scheme
     * @param path
     * @param query_json
     * @return
     */
    public boolean formatProtocolService(final String scheme, final String path, final String query_json){
        return formatProtocolService(DilutionsUriBuilder.buildUri(scheme, path, query_json));
    }

    public boolean formatProtocolService(final String scheme, final String path, final JSONObject query_json){
        return formatProtocolService(DilutionsUriBuilder.buildUri(scheme, path, query_json));
    }

    public boolean formatProtocolService(final String scheme, final String path, final org.json.JSONObject query_json){
        return formatProtocolService(scheme, path, query_json.toString());
    }

//    public boolean formatProtocolService(final String scheme, final String path, final Bundle query_json){
//        return formatProtocolService(scheme, path, JSON.toJSONString(query_json));
//    }

    public <K,V> boolean  formatProtocolService(final String scheme, final String path, final Map<K,V> map){
        String json = JSON.toJSONString(map);
        return formatProtocolService(DilutionsUriBuilder.buildUri(scheme, path, json));
    }

    /**
     * 带有监听的协议解析
     * @param uri_string
     * @param config
     */
    public boolean formatProtocolService(final String uri_string, final DilutionsConfig config){
        return formatProtocolService(uri_string, null,  config);
    }

    public boolean formatProtocolServiceWithCallback(final String uri_string, final DilutionsCallBack callBack){
        return formatProtocolService(uri_string, null, DilutionsConfigFactory.newBuilder(callBack, null, null).build());
    }

    public boolean formatProtocolServiceWithInterceptor(final String uri_string, final DilutionsInterceptor interceptor){
        return formatProtocolService(uri_string, null, DilutionsConfigFactory.newBuilder(null, interceptor, null).build());
    }

    public Map<String, ArrayList<String>> getJumpPathMap(){
        return dilutionsInstrument.getJumpPathMap();
    }

    public HashMap<String, ArrayList<String>> getMethodPathMap(){
        return dilutionsInstrument.getMethodPathMap();
    }

    public boolean checkUri(String s_uri){
        return dilutionsInstrument.checkUri(s_uri);
    }

    /**
     * 获得appmap
     * @return
     */
    public List<String> getAppMap(){
        return dilutionsInstrument.getAppMap();
    }

    /**
     * 获得appoutmap
     * @return
     */
//    public List<String> getAppOutMap(){
//        return dilutionsInstrument.getAppOutMap();
//    }

    /**
     * 类型检查map
     * @return
     */
    public Map<String, Class<?>> getCheckMap(){
        return dilutionsInstrument.getCheckMap();
    }

    public void addScheme(String scheme){
        getAppMap().add(scheme);
    }

    /**
     * 处理Http协议
     * @param uri
     */
    public boolean formatProtocolService(final Uri uri){
        return formatProtocolService(uri, null);
    }

    public boolean formatProtocolService(final Uri uri, final DilutionsConfig config){
        return formatProtocolService(uri.toString(), null, config);
    }

    public boolean formatProtocolServiceWithExtra(final String uri, final HashMap<String, Object> map){
        return formatProtocolService(uri, map, null);
    }

    public boolean formatProtocolServiceWithMap(final String uri, final HashMap<String, Object> map, final Map<String, Object> objectMap){
        return formatProtocolService(uri, map, objectMap, null);
    }

    public boolean formatProtocolService(final String uri, final HashMap<String, Object> map, final Map<String, Object> objectMap, final DilutionsConfig config){
        try {
            DilutionsManager httpProtocolManager = dilutionsInstrument.createHttpManager(Uri.parse(uri), map);
            dilutionsInstrument.dilutions(httpProtocolManager,config, objectMap);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(dilutionsInstrument.getDilutionsGlobalListener() != null){
            dilutionsInstrument.getDilutionsGlobalListener().onUnSupportUri(uri);
        }
        return false;
    }


    /**
     * 带有监听的http协议处理
     * @param uri
     * @param config
     */
    public boolean formatProtocolService(final String uri, final HashMap<String, Object> map, final DilutionsConfig config){
        return formatProtocolService(uri, map, null, config);
    }

    /**
     * 不带协议的解析
     * @param protocol
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T formatProtocolService(final Class<T> protocol){
        return formatProtocolService(protocol, null);
    }

    /**
     * 带有回调的协议解析
     * @param protocol
     * @param config
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T formatProtocolService(final Class<T> protocol, final DilutionsConfig config) {
        //只能代理接口
        Checker.validateManagerInterface(protocol);
        return (T) Proxy.newProxyInstance(protocol.getClassLoader(), new Class<?>[] { protocol },
                new InvocationHandler() {

                    @Override public Object invoke(Object proxy, Method method, Object... args)
                            throws Throwable {

                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        //获取代理方法
                        ProtocolManager managerMethod = dilutionsInstrument.createProtocolManager(method , args);
                        dilutionsInstrument.dilutions(managerMethod, config, null);
//                        return Void.class;
                        return Void.class;
                    }
                });
    }

    /**
     * 注册Activity
     */
    public void register(Object object){
        dilutionsInstrument.register(object);
    }

    /**
     * Activity的newIntent，暂时使用register
     * @param object
     */
    public void onNewIntent(Object object){
        dilutionsInstrument.register(object);
    }

    /**
     * 构造器
     */
    private static final class Builder {

        Context context;
        List<String> pathName;

        public Builder() {

        }

        public Builder context(Context context){
            this.context = context;
            return this;
        }

        public Builder pathName(List<String> pathName){
            this.pathName = pathName;
            return this;
        }

        public Dilutions build() {
            return new Dilutions(this);
        }
    }
}

package com.linhonghong.dilutions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.linhonghong.dilutions.data.DilutionsConfig;
import com.linhonghong.dilutions.data.DilutionsData;
import com.linhonghong.dilutions.data.DilutionsGlobalListener;
import com.linhonghong.dilutions.utils.DilutionsUtil;
import com.linhonghong.dilutions.interfaces.DilutionsCallBack;
import com.linhonghong.dilutions.interfaces.DilutionsInterceptor;
import com.linhonghong.dilutions.interfaces.DilutionsPathInterceptor;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Linhh on 16/9/2.
 */
public class DilutionsInstrument {
    public static final String TAG = "Dilutions_info";

    public static final String URI_CALL_CLASS = "uri-call-clazz";
    public static final String URI_CALL_PATH = "uri-call-path";
    public static final String URI_CALL_PARAM = "uri-call-param";
    public static final String URI_CALL_ALL = "uri-call-all";
    public static final String URI_FROM = "uri-from";
    public static final String SCHEME_IN = "DILUTIONS.SCHEME.IN";
    public static final String SCHEME_OUT = "DILUTIONS.SCHEME.OUT";
//    public static final String APP_SCHEME = "dilutions";//公共内部协议

//    public static final String COMPATIBLE_METHOD_NAME = "dilutionsCall";

    public static final String PATH_JUMP_FILE = "uiInterpreter.conf";

    private DilutionsGlobalListener mDilutionsGlobalListener;

    public static final int PARSER_TYPE_JUMP = 0;
    public static final int PARSER_TYPE_METHOD = 1;

    private final Map<Class<?>, ActivityDilutionsManager> managerCache = new LinkedHashMap<>();

    //方法缓存
    private final Map<Method, ProtocolManager> protocolCache = new LinkedHashMap<>();
    private final Map<String, ProtocolClazzORMethod> protocolClazzORMethodMap = new HashMap<>();

    private Map<String, ArrayList<String>> jumpPathMap;//pathMap, key = 协议/my/info, value = com.activity
    private final Map<String, Map<String, String>> jumpParamsMap = new HashMap<>();//paramsMap key = 协议/my/info , value = 参数以及检查类型

    private HashMap<String, ArrayList<String>> methodPathMap;//pathMap, key = 协议/my/info, value = com.activity
//    private final Map<String, Map<String, String>> methodParamsMap = new HashMap<>();//paramsMap key = 协议/my/info , value = 参数以及检查类型

    private final Map<String, Class<?>> checkMap = new HashMap<>();//参数合法性检查
    private final List<String> appMap = new ArrayList<>();//协议头map
    private final HashMap<String, ArrayList<DilutionsPathInterceptor>> mDilutionsPathInterceptor = new HashMap<>();
//    private final List<String> appOutMap = new ArrayList<>();//协议外

    private Context mContext;
    private List<String> mPathName;

    DilutionsInstrument(Context context, List<String> pathName){
        mContext = context;
//        mPathName = pathName;
    }

    void removeDilutionsPathInterceptor(String path, DilutionsPathInterceptor interceptor){
        ArrayList<DilutionsPathInterceptor> interceptors = mDilutionsPathInterceptor.get(path);
        if(interceptors != null) {
            interceptors.remove(interceptor);
        }
    }

    void setDilutionsPathInterceptor(String path ,DilutionsPathInterceptor dilutionsPathInterceptor){
        ArrayList<DilutionsPathInterceptor> interceptors = mDilutionsPathInterceptor.get(path);
        if(interceptors == null){
            interceptors = new ArrayList<>();
        }
        if(interceptors.size() == 0){
            interceptors.add(dilutionsPathInterceptor);
            mDilutionsPathInterceptor.put(path, interceptors);
            return;
        }
        for(int i = 0; i < interceptors.size(); i ++){
            DilutionsPathInterceptor interceptor = interceptors.get(i);
            if(interceptor.level() == dilutionsPathInterceptor.level()){
//                Log.e(TAG, "path:" + path + ",拦截器添加失败、已经存在" + interceptor.getClass() + ",级别:" + dilutionsPathInterceptor.level());
                return;
            }
            if(interceptor.level() < dilutionsPathInterceptor.level()){
                interceptors.add(i, dilutionsPathInterceptor);
                mDilutionsPathInterceptor.put(path, interceptors);
                return;
            }
        }
        interceptors.add(dilutionsPathInterceptor);
        mDilutionsPathInterceptor.put(path, interceptors);

    }

    void setDilutionsGlobalListener(DilutionsGlobalListener dilutionsGlobalListener){
        if(mDilutionsGlobalListener != null){
//            Log.e(TAG, "DilutionsGlobalListener is already exists.");
            return;
        }
        mDilutionsGlobalListener = dilutionsGlobalListener;
    }

    DilutionsGlobalListener getDilutionsGlobalListener(){
        return mDilutionsGlobalListener;
    }

    /**
     * 初始化
     */
    public void init() throws Exception{
        initUIProtocol();
        initMethodProtocol();
        if(jumpPathMap == null){
            jumpPathMap = new HashMap<>();
        }
        if(methodPathMap == null){
            methodPathMap = new HashMap<>();
        }
        initCheckMap();
        initAppMap();
        //解析path
//        if(mPathName != null) {
//            for (String pathname : mPathName) {
//                parserData(pathname, PARSER_TYPE_JUMP);
//            }
//        }
    }

    public Map<String, ArrayList<String>> getJumpPathMap(){
        return jumpPathMap;
    }

    public HashMap<String, ArrayList<String>> getMethodPathMap(){
        return methodPathMap;
    }

    /**
     * 初始化数据类型处理器
     */
    private void initCheckMap(){
        checkMap.put("int", Integer.class);
        checkMap.put("String", String.class);
        checkMap.put("long", Long.class);
        checkMap.put("double", Double.class);
        checkMap.put("boolean", Boolean.class);
        checkMap.put("float",Float.class);
//        checkMap.put("Serializable", Serializable.class);
//        int a = int.class.cast(checkMap);
    }

    /**
     * 初始化方法协议
     */
    private void initMethodProtocol(){
        try {
            Class methodMapClazz = Class.forName("com.linhonghong.dilutions.inject.support.DilutionsInjectMetas");
            Object obj = methodMapClazz.newInstance();

            Method method = methodMapClazz.getMethod("getMap");
            methodPathMap = (HashMap<String, ArrayList<String>>) method.invoke(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 初始化UI协议
     */
    private void initUIProtocol() throws Exception{
        try {
            Class methodMapClazz = Class.forName("com.linhonghong.dilutions.inject.support.DilutionsInjectUIMetas");
            Object obj = methodMapClazz.newInstance();

            Method method = methodMapClazz.getMethod("getMap");
            jumpPathMap = (HashMap<String, ArrayList<String>>) method.invoke(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Map<String, Class<?>> getCheckMap(){
        return checkMap;
    }

    public HttpProtocolManager createHttpManager(Uri uri, HashMap<String, Object> map) throws Exception{
        return new HttpProtocolManager.Builder(this, uri, map).build();
    }

    /**
     * 初始化appmap
     */
    private void initAppMap(){
        //默认
        appMap.add("dilutions");
    }

    /**
     * 获得appmap
     * @return
     */
    public List<String> getAppMap(){
        return appMap;
    }

    /**
     * 获得appoutmap
     * @return
     */
//    public List<String> getAppOutMap(){
//        return appOutMap;
//    }

//    Map<String, Map<String, String>> getParamsMap(int type) throws Exception{
//        switch (type){
//            case DilutionsValue.PROTOCOL_JUMP:
//                return jumpParamsMap;
//            case DilutionsValue.PROTOCOL_METHOD:
//                return methodParamsMap;
//            default:
//                throw new Exception("no params");
//        }
//    }

    /**
     * 旧的协议配置解析
     * 解析数据
     * @param name
     * @throws Exception
     */
    @Deprecated
    private void parserData(String name, int type) throws Exception{
//        InputStream pathis = getContext().getAssets().open(name);
//        Properties properties = new Properties();
//        properties.load(pathis);
//        Enumeration<Object> keys = properties.keys();
//        while (keys.hasMoreElements()) {
//            String key = (String) keys.nextElement();//协议
//            String value = properties.getProperty(key).trim();
//            if(DilutionsUtil.isEqual(SCHEME_IN, key)){
//                //配置头
//                parseScheme(appMap, value);
//            }else if(DilutionsUtil.isEqual(SCHEME_OUT, key)){
//                //配置头
////                parseScheme(appOutMap, value);
//            }else {
//                parseQuery(key, value, jumpPathMap);
////                switch (type) {
////                    case PARSER_TYPE_JUMP:
////                        //解析跳转
////                        parseQuery(key, value, jumpPathMap);
////                        break;
////                    case PARSER_TYPE_METHOD:
////                        //解析方法
////                        if (value.contains("#")) {
////                            //解析方法协议
////                            parseQuery(key, value, methodPathMap);
////                        } else {
////                            //兼容模式
////                            value = value.contains(COMPATIBLE_METHOD_PACKAGE) ? value : COMPATIBLE_METHOD_PACKAGE + value;
////                            value = value + "#" + COMPATIBLE_METHOD_NAME;
////                            parseQuery(key, value, methodPathMap);
////                        }
////                        break;
////                }
//            }
//        }
    }

    /**
     * 解析参数
     * @param key
     * @param value
     * @return true 解析成功,false失败
     */
    private boolean parseQuery(String key, String value, Map<String, ArrayList<String>> pathMap){
        String[] sp = value.split("\\(");//窃取数据
//        if (sp.length < 2) {
//            //只有一条数据,解析错误
////                throw new Exception("配置文件出错,位置:" + key + ",找不到参数");
//            Log.e(TAG,"Config error, config position: " + key + " ,dilutions couldn't solve this config key.");
//            return false;
//        }
        String path = sp[0].trim();
        ArrayList<String> list = new ArrayList<>();
        list.add(path);
//        if(type == PARSER_TYPE_JUMP) {
            pathMap.put(key, list);//将跳转位置加入path, class类
//        }else if(type == PARSER_TYPE_METHOD){
//            //方法处理
//            pathMap.put(key, path);//将跳转位置加入path, class类
//        }

//        String[] querys = sp[1].split("\\)");
//
//        //该处可以被优化
//        Map<String, String> queryMap = new HashMap<>();
//
//        if(querys.length > 0) {
//            //有参数
//            String query = querys[0].trim();//参数数据
//
//            String[] params = query.split(",");//解析参数数据,可能为空参数
//            for (String param : params) {
//                String[] data = param.trim().split(" ");//解析参数类型
//                if (data.length < 2) {
//                    //如果数据有问题就直接跳过
//                    continue;
//                }
//                //1为数据名,0为类型
//                queryMap.put(data[1].trim(), data[0].trim());//将参数数据加入表
//            }
//        }
//
//        paramsMap.put(key, queryMap);//将数据表加入参数数据,参数

        return true;
    }

    public void parseScheme(List<String> map , String value){
        String[] params = value.split(",");
        for(String param : params){
            String in = param.trim().substring(1, param.length() - 1);
            if(!map.contains(in)) {
                map.add(in);
            }
        }
    }

    /**
     * 检查数据合法性
     */
    boolean checkJumpUriSafe(String scheme, String path){
//        return jumpPathMap.containsKey(path);
//        if (scheme.contains(APP_SCHEME)) {
//            return jumpPathMap.containsKey(path);
//        }
        for (int i = 0; i < appMap.size(); ++i) {
            if (scheme.contains(appMap.get(i))) {
                return jumpPathMap.containsKey(path);
            }
        }
        return false;
//        return pathMap.containsKey(path);
    }

    public int getUriType(String path) throws Exception{
        if(jumpPathMap.containsKey(path)){
            return DilutionsValue.PROTOCOL_JUMP;
        }else if(methodPathMap.containsKey(path)){
            return DilutionsValue.PROTOCOL_METHOD;
        }
        throw new Exception("Uri协议出错,不存在[" + path + "]协议");
    }

    public int getUriType(String scheme, String path) throws Exception{
        if(checkJumpUriSafe(scheme,path)){
            return DilutionsValue.PROTOCOL_JUMP;
        }else if(checkMethodUriSafe(scheme, path)){
            return DilutionsValue.PROTOCOL_METHOD;
        }
        throw new Exception("Uri协议出错,不存在[" + scheme + "][" + path + "]协议");
    }

    /**
     * 检查方法数据合法性
     */
    boolean checkMethodUriSafe(String scheme, String path){
//        return methodPathMap.containsKey(path);
//        if (scheme.contains(APP_SCHEME)) {
//            return methodPathMap.containsKey(path);
//        }
        for (int i = 0; i < appMap.size(); ++i) {
            if (scheme.contains(appMap.get(i))) {
                return methodPathMap.containsKey(path);
            }
        }
        return false;
//        return pathMap.containsKey(path);
    }

    /**
     * 注册稀释器
     */
    public void register(Object object){
        Class<?> clazz = object.getClass();
        if(object instanceof Activity){
            //是Activity
            registerActivity(clazz, object);
        }else if(object instanceof Fragment){
            //是fragment
            registerFragment(clazz, object);
        }else{
            //无法识别
            return;
        }
    }

    private ActivityDilutionsManager getManager(Class<?> clazz){
        //得到当前Activity的所有对象
        ActivityDilutionsManager result;
        //不要使用缓存，因为所在的args是不同的
        synchronized (managerCache) {
            result = managerCache.get(clazz);
            if (result == null) {
                result = new ActivityDilutionsManager.Builder(clazz).build();
                managerCache.put(clazz, result);
            }
        }

        return result;
    }

    /**
     * 注册Activity
     * @param clazz
     * @param object
     */
    private void registerActivity(Class<?> clazz, Object object){
        //得到当前Activity的所有对象
        ActivityDilutionsManager result = getManager(clazz);
        try {
            if(result != null) {
                result.apply((Activity) object);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置http协议参数
     * @param jsonObject
     */
    public void createExtraParams(ArrayList<ParameterHanlder<?>> parameterHandlers, String path, JSONObject jsonObject) throws Exception{
        //2017.7.6修改,3.0特殊处理,如果jsonobJECT为空,不抛出错误
        if(jsonObject == null ){
            jsonObject = new JSONObject();
        }
        if(DilutionsUtil.isNull(path)){
            //JSON为空,无法解析
            throw new Exception("path is null");
        }
        //获得参数以及其类型,限制性做法
        for(Map.Entry<String, Object> entry : jsonObject.entrySet()){
            String key = entry.getKey();
//            Class<?> clazz = getParamsType(path, key);
//            if(clazz == null){
//                //没有配置转换类型
//                continue;
//            }
            Object value = jsonObject.get(key);
            if(value != null) {
                Class<?> clazz = value.getClass();
                ParameterHanlder.ExtraParams parameterHandler = new ParameterHanlder.ExtraParams<>(key, clazz.cast(value));
                parameterHandler.setType(clazz);
                parameterHandlers.add(parameterHandler);
            }else{
                //解析空值null
            }
        }
    }

    /**
     * 设置http协议参数
     * @param jsonObject
     */
    public void createExtraParams(Map<String, Map<String, String>> paramsMap, ArrayList<ParameterHanlder<?>> parameterHandlers, String path, JSONObject jsonObject){
        if(jsonObject == null || DilutionsUtil.isNull(path)){
            //JSON为空,无法解析
            return;
        }
        Map<String, String> params = paramsMap.get(path);
        if(params == null){
            return;
        }
        //获得参数以及其类型,限制性做法
        for(Map.Entry<String, String> entry : params.entrySet()){
            String key = entry.getKey();
            Class<?> clazz = getParamsType(path, key);
            if(clazz == null){
                //没有配置转换类型
                continue;
            }
            Object value = jsonObject.get(key);
            if(value != null) {
                ParameterHanlder.ExtraParams parameterHandler = new ParameterHanlder.ExtraParams<>(key, clazz.cast(value));
                parameterHandler.setType(clazz);
                parameterHandlers.add(parameterHandler);
            }else{
                //解析空值
            }
        }
    }

    /**
     * 注册Fragment
     * @param clazz
     * @param object
     */
    private void registerFragment(Class<?> clazz, Object object){
        //得到当前Fragment的所有对象
        ActivityDilutionsManager result = getManager(clazz);
        try {
            if(result != null) {
                result.apply((Fragment) object);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean checkUri(String s_uri){
        boolean result = false;
        if(mDilutionsGlobalListener != null){
            result = mDilutionsGlobalListener.onCheckUri(s_uri);
        }
        if(result){
            return result;
        }
        try {
            Uri uri = Uri.parse(s_uri);
            String scheme = uri.getScheme();
            String path = uri.getPath();
            result = checkJumpUriSafe(scheme, path);
            if (!result) {
                result = checkMethodUriSafe(scheme, path);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 创建M层方法管理器
     * @param method
     * @param args
     * @return
     */
    ProtocolManager createProtocolManager(Method method, Object... args) {
        ProtocolManager result;
        //不要使用缓存，因为所在的args是不同的
        synchronized (protocolCache) {
            result = protocolCache.get(method);
            if (result == null) {
                result = new ProtocolManager.Builder(this, method).build();
                protocolCache.put(method, result);
            }
            result.args(args);
        }
        return result;
    }
//
//    /**
//     * 创建M层方法管理器(网络协议)
//     * @param method
//     * @param args
//     * @return
//     */
//    ProtocolManager createProtocolManager(Method method, HashMap<String, Object> args) {
//        ProtocolManager result;
//        //不要使用缓存，因为所在的args是不同的
//        synchronized (protocolCache) {
//            result = protocolCache.get(method);
//            if (result == null) {
//                result = new ProtocolManager.Builder(this, method).build();
//                protocolCache.put(method, result);
//            }
//            result.args(args);
//        }
//        return result;
//    }

    /**
     * 通过path获取需要跳转的class
     * @param path
     * @return
     */
    public ProtocolClazzORMethod getClazz(int protocolType, String path) throws Exception{
        if(protocolClazzORMethodMap.containsKey(path)){
            return protocolClazzORMethodMap.get(path);
        }
        ProtocolClazzORMethod protocolClazzORMethod = null;
        //只会被调用一次
        switch (protocolType){
            case DilutionsValue.PROTOCOL_JUMP:
                //                String enterAnim, exitAnim = null;
                ArrayList<String> list = jumpPathMap.get(path);
//                enterAnim = list.get(1);
//                exitAnim = list.get(2);
                Class<?> jumpClazz = Class.forName(list.get(0));
                protocolClazzORMethod = new ProtocolClazzORMethod(jumpClazz, null , path, list, null);
                protocolClazzORMethodMap.put(path, protocolClazzORMethod);
                return protocolClazzORMethod;
            case DilutionsValue.PROTOCOL_METHOD:
                //返回方法跳转类和方法名sp[0]=类,sp[1]=方法
                Class<?> methodClazz = Class.forName(methodPathMap.get(path).get(0));
                //取出类参数类型
                String j = methodPathMap.get(path).get(2);
                String[] param_type = null;
                if(j == null || j.length() == 0){
                    param_type = new String[]{};
                }else {
                    param_type = j.split("#");
                }
                Class<?>[] classes = new Class[param_type.length];
                for(int i = 0 ;i < param_type.length; i++){
                    classes[i] = DilutionsUtil.getParamsClass(param_type[i]);
                }
                protocolClazzORMethod = new ProtocolClazzORMethod(methodClazz, methodPathMap.get(path).get(1), path, methodPathMap.get(path),classes);
                protocolClazzORMethodMap.put(path, protocolClazzORMethod);
                return protocolClazzORMethod;
            default:
                throw new Exception("协议错误");
        }

    }

    /**
     * 根据配置名获得参数名
     * @param param
     * @return
     */
//    public String getParams(int type, String param, String path){
//        //参数检查,如果参数不在配置中就提示错误
//        //只会被调用一次
//        switch (type){
//            case DilutionsValue.PROTOCOL_JUMP:
//                if(!jumpParamsMap.get(path).containsKey(param)){
//                    Log.w(TAG,path + " " + param + " 参数缺失");
//                }
//                return param;
//            case DilutionsValue.PROTOCOL_METHOD:
//                if(!methodParamsMap.get(path).containsKey(param)){
////                    throw new Exception("参数错误");
//                    Log.w(TAG,path + " " + param + " 参数缺失");
//                }
//                return param;
//            default:
//                return param;
////                throw new Exception("协议错误");
//        }
//    }

    /**
     * 获取参数类型
     * @param path
     * @param param
     * @return
     */
    public Class<?> getParamsType(String path, String param){
        Map<String ,String> params = jumpParamsMap.get(path);
        if(params == null){
            return null;
        }
        String type = params.get(param);
        if(DilutionsUtil.isNull(type)){
            return null;
        }
        return checkMap.get(type);
    }

    /**
     * 处理协议
     * @param protocolManager
     */
    void dilutions(DilutionsManager protocolManager, DilutionsConfig config, Map<String,Object> extraMap) throws Exception{
        protocolManager.apply();
        DilutionsData data = protocolManager.getDilutionsBuilder().getDilutionsData();
        DilutionsCallBack callBack = null;
        DilutionsInterceptor interceptor = null;
        String path = protocolManager.getDilutionsBuilder().getPath();
        String u = "";
        if(data.getUri() != null){
            u = data.getUri().toString();
        }
//        Log.e(TAG, "path:" + path + "协议开始执行。" + u);
        if(mDilutionsGlobalListener != null && mDilutionsGlobalListener.onIntercept(data)){
//            Log.e(TAG, "path:" + path + "被拦截,拦截者_全局拦截:" + interceptor.getClass());
            return;
        }
        //执行协议拦截器
        ArrayList<DilutionsPathInterceptor> interceptors = mDilutionsPathInterceptor.get(path);
        if(interceptors != null && interceptors.size() > 0){

            for(DilutionsPathInterceptor interceptor1 : interceptors){
                if(interceptor1.interceptor(data)){
//                    Log.e(TAG, "path:" + path + "被局部拦截,拦截者:" + interceptor1.getClass() + ",level:" + interceptor1.level());
                    return;
                }
            }
        }
        //处理协议
//        Log.d("dilutions", "Process:" + path);
        if(config != null){
            callBack = config.getDilutionsCallBack();
            interceptor = config.getDilutionsInterceptor();
            data.setWhat(config.getWhat());
        }

        if(interceptor != null && interceptor.interceptor(data)){
//            Log.e(TAG, "path:" + path + "被拦截,拦截者_发起协议者:" + interceptor.getClass());
            //拦截
            return;
        }
//        try {
            //TODO:捕获的原因是因为想让callback继续执行
            //处理协议最后逻辑
        if (protocolManager.getProtocolType() == DilutionsValue.PROTOCOL_JUMP) {
            ArrayList<String> list = data.getList();
            int enterAnim = 0;
            int exitAnim = 0;
            try{
                String ea = list.get(1);
                enterAnim = Integer.valueOf(ea);
            }catch (Exception e){
                e.printStackTrace();
            }

            try{
                String ea = list.get(2);
                exitAnim = Integer.valueOf(ea);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(enterAnim != 0 || exitAnim != 0){
                //如果有一个拥有动画，则
                ActivityOptionsCompat transitionActivityOptions =
                        ActivityOptionsCompat.makeCustomAnimation(mContext,enterAnim, exitAnim);
                ActivityCompat.startActivities(mContext,
                        new Intent[]{data.getIntent()}, transitionActivityOptions.toBundle());
            }else {
                mContext.startActivity(data.getIntent());
            }
        } else if (protocolManager.getProtocolType() == DilutionsValue.PROTOCOL_METHOD) {
                //方法解析
//                if(!methodMap.containsKey(path)) {
                    //缓存处理
                    Class<?> clazz = protocolManager.getProtocolClazzORMethod().clazz;
                    String methodName = protocolManager.getProtocolClazzORMethod().methodName;

                    Object obj = protocolManager.getProtocolClazzORMethod().clazz.newInstance();

                    //取出类参数类型
                    String j = protocolManager.getProtocolClazzORMethod().mParamsList.get(2);
//                    String[] param_type = j.split("#");
//                    Class<?>[] classes = new Class[param_type.length];
//                    for(int i = 0 ;i < param_type.length; i++){
//                        classes[i] = DilutionsUtil.getParamsClass(param_type[i]);
//                    }

                    Method method = clazz.getDeclaredMethod(methodName, protocolManager.getProtocolClazzORMethod().mClasses);
                    if(j.equals("")){
                        //空参数
                        method.invoke(obj);
                    }else{
                        //需要缓存
                        HashMap<Integer, String> indexs = new HashMap<>();
                        for(int i = 3; i < protocolManager.getProtocolClazzORMethod().mParamsList.size(); i ++){
                            String[] s = protocolManager.getProtocolClazzORMethod().mParamsList.get(i).split("=");
                            indexs.put(Integer.valueOf(s[0]),s[1]);
                        }
                        String[] params_type = j.split("#");
                        Object[] objs = new Object[params_type.length];
                        Bundle bundle = data.getIntent().getExtras();
                        for(int i = 0 ;i < params_type.length; i++){

                            //该位置存在协议名，所以这个地方要解析
                            //example: 0=tree,i = 0, indexs.get(i) = tree
                            if (indexs.containsKey(i)) {
                                //存在字段
                                Class type = protocolManager.getProtocolClazzORMethod().mClasses[i];
                                Object object = bundle.get(indexs.get(i));
                                if(object == null){
                                    //没有该参数，判断是否需要从外部传递的取参
                                    if(extraMap != null){
                                        object = extraMap.get(indexs.get(i));
                                    }
                                    if(object == null) {
                                        object = DilutionsUtil.getParams(params_type[i]);
                                    }
                                }else {
                                    if (type == String.class) {
                                        object = String.valueOf(object);
                                    } else if (type == Integer.class) {
                                        object = Integer.valueOf(String.valueOf(object));
                                    } else if (type == int.class) {
                                        object = Integer.valueOf(String.valueOf(object)).intValue();
                                    } else if (type == Long.class) {
                                        object = Long.valueOf(String.valueOf(object));
                                    } else if (type == long.class) {
                                        object = Long.valueOf(String.valueOf(object)).longValue();
                                    } else if (type == Double.class) {
                                        object = Double.valueOf(String.valueOf(object));
                                    } else if (type == double.class) {
                                        object = Double.valueOf(String.valueOf(object)).doubleValue();
                                    } else if (type == Boolean.class) {
                                        object = Boolean.valueOf(String.valueOf(object));
                                    } else if (type == boolean.class) {
                                        object = Boolean.valueOf(String.valueOf(object)).booleanValue();
                                    } else if (type == Float.class) {
                                        object = Float.valueOf(String.valueOf(object));
                                    } else if (type == float.class) {
                                        object = Float.valueOf(String.valueOf(object)).floatValue();
                                    } else {
                                        if (object instanceof JSON) {
                                            //是json
                                            object = JSONObject.parseObject(((JSON) object).toJSONString(), protocolManager.getProtocolClazzORMethod().mClasses[i]);

//                                            object = ((JSON) object).toJavaObject(protocolManager.getProtocolClazzORMethod().mClasses[i]);
                                        }
                                    }
                                }
                                objs[i] = object;

                            } else {
                                objs[i] = DilutionsUtil.getParams(params_type[i]);
                            }
                        }
                        Object object = method.invoke(obj,objs);
                        data.setResult(object);
//                        Log.i(TAG, "方法执行返回:" + object);
                    }

//                    Class<?>[] typeArgs = new Class[1];
//                    typeArgs[0] = DilutionsData.class;
//                    //TODO:需要做缓存优化处理?
//                    //2017.3.29更新寻找super的方法
//                    try {
//                        method = clazz.getDeclaredMethod(methodName, typeArgs);
//                    }catch (Exception e){
//                        e.printStackTrace();
                        //寻找父类的方法,因为有可能被调用方法存在于父类
//                        Class<?> round_clazz = clazz.getSuperclass();
//                        while(method == null && round_clazz != Object.class){
//                            method = findMethod(round_clazz, methodName, typeArgs);
//                            round_clazz = round_clazz.getSuperclass();
//                        }
//                    }
//                    if(method == null){
//                        throw new Exception("dilutions could't find method, method is null");
//                    }
//                    DilutionsMethodData dilutionsMethodData = new DilutionsMethodData(obj, method);
//                    methodMap.put(path, dilutionsMethodData);
//                }
//                DilutionsMethodData methodData = methodMap.get(path);
//                dilutionsMethodData.method.invoke(dilutionsMethodData.obj, data);
            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        //callback
        if(callBack != null){
            callBack.onDilutions(data);
        }

        if(mDilutionsGlobalListener != null){
            mDilutionsGlobalListener.onCallBack(data);
        }

    }

    private Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes){
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得builder
     * @return
     */
    DilutionsBuilder getDilutionsBuilder(){
        return new DilutionsBuilder(mContext);
    }

    /**
     * 获得context
     * @return
     */
    public Context getContext(){
        return mContext;
    }
}

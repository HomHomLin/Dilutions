package com.linhonghong.dilutions;

import android.content.Intent;

import com.linhonghong.dilutions.annotations.ExtraParam;
import com.linhonghong.dilutions.annotations.PassNull;
import com.linhonghong.dilutions.annotations.ProtocolPath;
import com.linhonghong.dilutions.utils.Checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 协议管理器
 * Created by Linhh on 16/11/30.
 */
public class ProtocolManager<T> implements DilutionsManager<T> {

    //基础数据
    ArrayList<ParameterHanlder<?>> parameterHandlers;
    int methodIndex = 0;
    int protocolType;
    DilutionsBuilder dilutionsBuilder;
    ProtocolClazzORMethod protocolClazzORMethod;

//    String[] args;

    ProtocolManager(Builder<T> builder) {
        this.parameterHandlers = builder.parameterHandlers;
        this.methodIndex = builder.methodIndex;
        this.protocolClazzORMethod = builder.protocolClazzORMethod;
        this.protocolType = builder.protocolType;
        this.dilutionsBuilder = builder.dilutionsBuilder;
    }

    /**
     * 生效请求配置
     * @throws Exception
     */
    public void apply() throws Exception{
        for(int i = methodIndex; i < parameterHandlers.size(); i ++){
            parameterHandlers.get(i).apply(dilutionsBuilder);
        }
    }

    @Override
    public DilutionsBuilder getDilutionsBuilder() {
        return dilutionsBuilder;
    }

    @Override
    public ProtocolClazzORMethod getProtocolClazzORMethod() {
        return protocolClazzORMethod;
    }

    @Override
    public int getProtocolType() {
        return protocolType;
    }

    public Intent intent(){
        return dilutionsBuilder.getDilutionsData().getIntent();
    }

    /**
     * 设置值
     * @param args
     */
    public void args(Object... args){
        //保存数据
        for(int i = methodIndex, x = 0 ; i < parameterHandlers.size(); i ++, x ++){
            parameterHandlers.get(i).setValue(args[x]);
        }
    }

    /**
     * 网络协议
     * @param map
     */
    public void args(HashMap<String, Object> map){
        //保存数据
        for(int i = methodIndex, x = 0 ; i < parameterHandlers.size(); i ++, x ++){
            ParameterHanlder<?> parameterHanlder = parameterHandlers.get(i);
            parameterHandlers.get(i).setValue(map.get(parameterHanlder.getName()));
        }
    }

    static final class Builder<T> {

        final DilutionsInstrument instrument;
        final Method method;
        final Annotation[] methodAnnotations;
        final Annotation[][] parameterAnnotationsArray;
        final Type[] parameterTypes;

        ProtocolClazzORMethod protocolClazzORMethod;//跳转目标

        //参数
        ArrayList<ParameterHanlder<?>> parameterHandlers;

        DilutionsBuilder dilutionsBuilder;

        //保存方法所有的注解数量
        int methodIndex = 0;

        int protocolType = DilutionsValue.PROTOCOL_JUMP;

        public Builder(DilutionsInstrument instrument, Method method) {
            this.instrument = instrument;
            this.method = method;
            //方法注解
            this.methodAnnotations = method.getAnnotations();
            //获得参数类型
            this.parameterTypes = method.getGenericParameterTypes();
            //参数注解
            this.parameterAnnotationsArray = method.getParameterAnnotations();

            this.dilutionsBuilder = instrument.getDilutionsBuilder();
        }

        /**
         * 解析方法注解
         * @param annotation
         */
        private void parseMethodAnnotation(Annotation annotation) throws Exception{
            if (annotation instanceof ProtocolPath) {
                parseMethodPath(((ProtocolPath) annotation).value());
            }
        }

        /**
         * 根据名字找到API
         * @param value
         */
        private void parseMethodPath(String value) throws Exception{

            protocolType = instrument.getUriType(value);

            protocolClazzORMethod = instrument.getClazz(protocolType, value);

            if(protocolClazzORMethod == null){
                //错误
                throw methodError("clazz is null");
            }

            //设置dilutions的跳转类
            dilutionsBuilder.setClazz(protocolType, value, protocolClazzORMethod);

        }

        public ProtocolManager build() {
            //存储参数处理器
            parameterHandlers = new ArrayList<>();

            //解析当前方法的所有annotation
            for (Annotation annotation : methodAnnotations) {
                //当前只会有一个api，所以实际上不需要for循环
                try {
                    parseMethodAnnotation(annotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            methodIndex = parameterHandlers.size();

            //解析参数annotation
            int parameterCount = parameterAnnotationsArray.length;
//            int annotationsCount = 0;
            //计算annotations数量。实际上需要这个吗？这里可以优化，但这里不这样做很容易oob
//            for(int i = 0 ; i < parameterCount; i ++){
//                if(parameterAnnotationsArray[i].length > 0) {
//                    annotationsCount ++;
//                }
//            }
            //真实计算
            for (int p = 0; p < parameterCount; p++) {
                //获取参数数据类型
                Type parameterType = parameterTypes[p];
                //判断是否是不能处理的数据
                if (Checker.hasUnresolvableType(parameterType)) {
                    throw parameterError(p, "Parameter type must not include a type variable or wildcard: %s",
                            parameterType);
                }

                //当前参数的annotation
                Annotation[] parameterAnnotations = parameterAnnotationsArray[p];
                if (parameterAnnotations == null) {
                    throw parameterError(p, "No annotation found.");
                }

//                if(parameterAnnotationsArray[p].length > 0) {
                //这是个annotations，解析
                ParameterHanlder<?> parameterHandler = parseParameter(p, parameterType, parameterAnnotations);
                parameterHandlers.add(parameterHandler);
//                    annotationsCount ++;
//                }
//                else{
//                    //非annotations
//                    parameterHandlers[p] = null;
//                }
            }

            dilutionsBuilder.setFrom(DilutionsValue.DILUTIONS_PROXY);//代理跳转

            return new ProtocolManager(this);
        }

        private ParameterHanlder<?> parseParameterAnnotation(
                int p, Type type, Annotation annotation) {
            if (annotation instanceof ExtraParam) {
                //解析Get参数
                ExtraParam query = (ExtraParam) annotation;
                String name = query.value();
                //暂时不支持数组类型，如果需要数组在增加
                Class<?> rawParameterType = Checker.getRawType(type);
                if (Iterable.class.isAssignableFrom(rawParameterType)) {
                    //TODO:实现数组
                    throw parameterError(p, "not support array current");
                } else if (rawParameterType.isArray()) {
                    //TODO:实现数组
                    throw parameterError(p, "not support array current");
                } else {
                    //获取真实参数
//                    name = instrument.getParams(protocolType, name, protocolClazzORMethod.mPath);
                    ParameterHanlder.ExtraParams parameterHandler = new ParameterHanlder.ExtraParams<>(name);
                    parameterHandler.setType(rawParameterType);
                    return parameterHandler;
                }

            }
            //没有能识别的参数，如果出现这个说明出错了
            return null;
        }

        private ParameterHanlder<?> parseParameter(
                int p, Type parameterType, Annotation[] annotations) {
            ParameterHanlder<?> result = null;
            boolean nullignore = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof PassNull) {
                    //如果是nullignore
                    nullignore =  true;
                    continue;
                }
                ParameterHanlder<?> annotationAction = parseParameterAnnotation(
                        p, parameterType, annotation);

                if (annotationAction == null) {
                    continue;
                }

                if (result != null) {
                    //重复annotations
                    throw parameterError(p, "only support one annotations");
                }

                result = annotationAction;
            }

            if (result == null) {
                throw parameterError(p, "No annotation found");
            }

            result.setPassNull(nullignore);

            return result;
        }

        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            message = String.format(message, args);
            return new IllegalArgumentException(message
                    + "\n    for method "
                    + method.getDeclaringClass().getSimpleName()
                    + "."
                    + method.getName(), cause);
        }

        private RuntimeException parameterError(int p, String message, Object... args) {
            return methodError(message + " (parameter #" + (p + 1) + ")", args);
        }
    }
}

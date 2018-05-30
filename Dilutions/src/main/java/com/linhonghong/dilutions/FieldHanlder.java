package com.linhonghong.dilutions;

import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.linhonghong.dilutions.utils.DilutionsUtil;

import java.lang.reflect.Field;

/**
 * Created by Linhh on 16/9/2.
 */
abstract class FieldHandler<T> {
    abstract void apply(Object object, Bundle bundle) throws Exception;
    abstract String name();

    static final class ExtraHandler<T> extends FieldHandler<T> {
        private final Field field;
        private final String name;
        public ExtraHandler(Field field, String name){
            this.field = field;
            this.name = name;
        }

        @Override
        void apply(Object object, Bundle bundle) throws Exception {
            Object key = bundle.get(name);
            if(key != null) {
                field.setAccessible(true);
                field.set(object, key);
            }
        }

        @Override
        String name() {
            return this.name;
        }
    }

    static final class FragmentargHandler<T> extends FieldHandler<T> {
        private final Field field;
        private final String name;
        public FragmentargHandler(Field field, String name){
            this.field = field;
            this.name = name;
        }

        @Override
        void apply(Object object, Bundle bundle) throws Exception {
            String jsonParam = bundle.getString(DilutionsInstrument.URI_CALL_PARAM);
            if(DilutionsUtil.isNull(jsonParam)){
                //没有找到json
                Object key = bundle.get(name);
                if(key != null) {
                    field.setAccessible(true);
                    field.set(object, key);
                }
                return;
            }
            JSONObject jsonObject = JSON.parseObject(jsonParam);
            if(jsonObject == null){
                //转换失败
                return;
            }
            jsonObject = jsonObject.getJSONObject(DilutionsValue.VAL_PARAMS);
            if(jsonObject == null){
                return;
            }
            Object key = jsonObject.get(name);

            if(key != null) {
                if(key instanceof JSONObject){
                    //转换为对象
                    key = JSON.parseObject(jsonObject.get(name).toString(),field.getType());
                }
                field.setAccessible(true);
                field.set(object, key);
            }
        }

        @Override
        String name() {
            return this.name;
        }
    }

    static final class ActivityProtocolExtraHandler<T> extends FieldHandler<T> {
        private final Field field;
        private final String name;
        public ActivityProtocolExtraHandler(Field field, String name){
            this.field = field;
            this.name = name;
        }

        @Override
        void apply(Object object, Bundle bundle) throws Exception {
            //获取传递的json
            String jsonParam = bundle.getString(DilutionsInstrument.URI_CALL_PARAM);
            if(DilutionsUtil.isNull(jsonParam)){
                //没有找到json
                return;
            }
            JSONObject jsonObject = JSON.parseObject(jsonParam);
            if(jsonObject == null){
                //转换失败
                return;
            }
            jsonObject = jsonObject.getJSONObject(DilutionsValue.VAL_PARAMS);
            if(jsonObject == null){
                return;
            }
            Object key = jsonObject.get(name);

            if(key != null) {
                if(key instanceof JSONObject){
                    //转换为对象
                    key = JSON.parseObject(jsonObject.get(name).toString(),field.getType());
                }
                field.setAccessible(true);
                field.set(object, key);
            }
        }

        @Override
        String name() {
            return this.name;
        }
    }

    static final class ActivityProtocolFromHandler<T> extends FieldHandler<T> {
        private final Field field;
        private final String name;
        public ActivityProtocolFromHandler(Field field, String name){
            this.field = field;
            this.name = name;
        }

        @Override
        void apply(Object object, Bundle bundle) throws Exception {
            //获取传递的path
            String path = bundle.getString(DilutionsInstrument.URI_FROM);
            field.setAccessible(true);
            field.set(object, path);
        }

        @Override
        String name() {
            return this.name;
        }
    }

    static final class ActivityProtocolPathHandler<T> extends FieldHandler<T> {
        private final Field field;
        private final String name;
        public ActivityProtocolPathHandler(Field field, String name){
            this.field = field;
            this.name = name;
        }

        @Override
        void apply(Object object, Bundle bundle) throws Exception {
            //获取传递的path
            String path = bundle.getString(DilutionsInstrument.URI_CALL_PATH);
            field.setAccessible(true);
            field.set(object, path);
        }

        @Override
        String name() {
            return this.name;
        }
    }
}

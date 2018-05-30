package com.linhonghong.dilutions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.linhonghong.dilutions.annotations.ActivityExtra;
import com.linhonghong.dilutions.annotations.ActivityProtocolExtra;
import com.linhonghong.dilutions.annotations.ActivityProtocolPath;
import com.linhonghong.dilutions.annotations.FragmentArg;
import com.linhonghong.dilutions.annotations.ProtocolFrom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * activity的稀释器管理器
 * Created by Linhh on 16/9/2.
 */
public class ActivityDilutionsManager {

    public final ArrayList<FieldHandler> fieldHandlers;

    ActivityDilutionsManager(Builder builder) {
        this.fieldHandlers = builder.fieldHandlers;
    }

    public void apply(Activity activity) throws Exception{
        Intent intent = activity.getIntent();
        if(intent == null){
            throw new Exception("intent is null");
        }
        Bundle bundle = intent.getExtras();
        if(bundle == null){
            throw new Exception("bundle is null");
        }
        for(FieldHandler fieldHandler : fieldHandlers){
            fieldHandler.apply(activity, bundle);
        }
    }

    public void apply(Fragment fragment) throws Exception{

        Bundle bundle = fragment.getArguments();
        if(bundle == null){
            throw new Exception("bundle is null");
        }
        for(FieldHandler fieldHandler : fieldHandlers){
            fieldHandler.apply(fragment, bundle);
        }
    }

    static final class Builder {

        //该类的所有具有变量
        private final ArrayList<FieldHandler> fieldHandlers = new ArrayList<>();

        private final Class<?> clazz;

        public Builder(Class<?> clazz) {
            this.clazz = clazz;
        }

        private void parseFields(){
            Field[] localfield = clazz.getDeclaredFields();
            for(Field field : localfield){
                //得到变量的注解
                Annotation[] annotations = field.getDeclaredAnnotations();
                //解析注解
                parseAnnotations(field, annotations);
            }
        }

        private void parseAnnotations(Field field , Annotation[] annotations){
            for(Annotation annotation: annotations){
                FieldHandler fieldHandler = parseAnnotation(field, annotation);
                if(fieldHandler == null){
                    continue;
                }

                //解析成功
                fieldHandlers.add(fieldHandler);
            }
        }

        private FieldHandler parseAnnotation(Field field , Annotation annotation ){

            if(annotation instanceof ActivityExtra){
                //如果是Activity的注解
                ActivityExtra extra = (ActivityExtra)annotation;
                return new FieldHandler.ExtraHandler(field, extra.value());
            }else if(annotation instanceof FragmentArg){
                //如果是Fragment的注解
                FragmentArg fragmentArg = (FragmentArg)annotation;
                return new FieldHandler.FragmentargHandler(field, fragmentArg.value());
            }else if(annotation instanceof ActivityProtocolExtra){
                //协议注解
                ActivityProtocolExtra activityProtocolExtra = (ActivityProtocolExtra)annotation;
                return new FieldHandler.ActivityProtocolExtraHandler(field, activityProtocolExtra.value());
            }else if(annotation instanceof ActivityProtocolPath){
                //协议
                ActivityProtocolPath activityProtocolPath = (ActivityProtocolPath)annotation;
                return new FieldHandler.ActivityProtocolPathHandler(field, activityProtocolPath.value());

            }else if(annotation instanceof ProtocolFrom){
                //协议
                ProtocolFrom protocolFrom = (ProtocolFrom)annotation;
                return new FieldHandler.ActivityProtocolFromHandler<>(field, protocolFrom.value());

            }

            return null;
        }

        public ActivityDilutionsManager build() {
            parseFields();
            return fieldHandlers.size() > 0 ? new ActivityDilutionsManager(this) : null;
        }
    }
}

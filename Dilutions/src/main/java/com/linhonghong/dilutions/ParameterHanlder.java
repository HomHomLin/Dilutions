package com.linhonghong.dilutions;

import com.linhonghong.dilutions.utils.DilutionsUtil;

/**
 * Created by Linhh on 16/11/30.
 */

public abstract class ParameterHanlder<T> {
    abstract void apply(DilutionsBuilder builder) throws Exception;
    abstract void setValue(Object value);
    abstract String getName();
    abstract void setType(Class<?> type);
    abstract void setPassNull(boolean nullIgnore);

    static final class ExtraParams<T> extends ParameterHanlder<T> {
        private final String name;

        private Object value;
        private Class<?> type;

        public boolean passnull = false;//用于判断是否提交空该字段

        ExtraParams(String name){
            this.name = name;
        }

        ExtraParams(String name, Object value) {
            this.name = name;
            this.value = value;
        }


        @Override void apply(DilutionsBuilder builder) throws Exception {
            //为null做处理，方便那些懒惰的程序员
            if(passnull){
                if(value instanceof String && DilutionsUtil.isNull(String.valueOf(value))){
                    return;
                }
                if(value == null){
                    return;
                    //如果是nullIgnore并且该字段为空则不提交该字段
                }
            }
            builder.addParams(name, DilutionsUtil.formatString(value), type);
        }

        @Override
        void setValue(Object value) {
            this.value = value;
        }

        @Override
        String getName() {
            return name;
        }

        @Override
        void setType(Class<?> type) {
            this.type = type;
        }

        @Override
        void setPassNull(boolean passnull) {
            this.passnull = passnull;
        }
    }
}

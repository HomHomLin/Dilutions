package com.linhonghong.dilutions;

/**
 * Created by Linhh on 2017/6/26.
 */

public class DilutionsUtils {
    public static final String DILUTIONS_METHOD_EXTRA = "dilutions_method_params_extra";
    public static String getType(String typeS){
        if("Z".equals(typeS)){
            return "Boolean";
        }
        if("B".equals(typeS)){
            return "Boolean";
        }
        if("C".equals(typeS)){
            return "Char";
        }
        if("S".equals(typeS)){
            return "Short";
        }
        if("I".equals(typeS)){
            return "Integer";
        }
        if("F".equals(typeS)){
            return "Float";
        }
        if("D".equals(typeS)){
            return "Double";
        }
        if("J".equals(typeS)){
            return "Long";
        }

        if("Ljava/lang/String".equals(typeS)){
            return "String";
        }
        return "Object";
    }
}

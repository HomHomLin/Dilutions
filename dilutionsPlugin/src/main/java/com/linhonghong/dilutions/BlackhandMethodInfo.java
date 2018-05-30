package com.linhonghong.dilutions;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Type;

/**
 * Created by Linhh on 17/6/15.
 */

public class BlackhandMethodInfo {
    public ArrayList<MethodAnnotationsInfo> mMethodAnnotations = new ArrayList<>();
    public String mMethodName;
    public List<Type> mMethodParms;
    public String mDesc;
    public BlackhandMethodInfo(){

    }
    public BlackhandMethodInfo(String methodName ,List<Type> methodParms,String desc){
        mMethodName = methodName;
        mMethodParms = methodParms;
        mDesc = desc;
    }

    public String getDesc(){
        return mDesc;
    }

    public String getMethodName(){
        return mMethodName;
    }

    public String getJavaMethodParams(){
        String result = "";
        for(int i = 0; i < mMethodParms.size(); i ++){
            result = result + mMethodParms.get(i).getClassName();

//            result = result + DilutionsUtils.getType(mMethodParms.get(i).toString());
            if(i != (mMethodParms.size() - 1)){
                //最后一个
                result = result + "#";
            }
        }
        return result;
    }

    public String getMethodParms(){
        String stype = "(";
        for(Type type : mMethodParms){
            stype = stype + type.toString();
        }
        stype = stype + ")";
        return stype;
    }

    @Override
    public String toString() {
        String stype = "";
        for(Type type : mMethodParms){
            stype = stype + type.toString() + "---";
        }
        for(MethodAnnotationsInfo type : mMethodAnnotations){
            stype = stype + type.toString() + "---";
        }
        return mMethodName + ":" + stype + "[:]" + mDesc;
    }
}

package com.linhonghong.dilutions;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Linhh on 17/6/22.
 */

public class MethodAnnotationsInfo {
    public int mParameter;
    public String mDesc;
    public boolean mVisible;
    public AnnotationNode mNode;
    public MethodAnnotationsInfo(int parameter, String desc, boolean visible, AnnotationNode node){
        mParameter = parameter;
        mDesc = desc;
        mVisible = visible;
        mNode = node;
    }

    public int getIndex(){
        return mParameter;
    }

    public String getParamName(){
        List vl = mNode.values;
        if (vl != null) {
            //vl.get(0).toString() 属性名字
            return (String)vl.get(1);
        }
        return "";
    }

    @Override
    public String toString() {
        List vl = mNode.values;
        String node = "";
        if (vl != null) {
            //vl.get(0).toString() 属性名字
            node = vl.get(0).toString() + "=" + vl.get(1) + ";" + node;
        }
        return mParameter  + ";" + node;
//        return mParameter + ";" + mDesc + ";" + mVisible + ";" + node;
    }
}

package com.linhonghong.dilutions;


import com.linhonghong.dilutions.annotations.ActivityProtocol;
import com.linhonghong.dilutions.annotations.CustomAnimation;
import com.linhonghong.dilutions.annotations.MethodExtra;
import com.linhonghong.dilutions.annotations.MethodParam;
import com.linhonghong.dilutions.annotations.MethodProtocol;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Linhh on 17/6/8.
 */

public class BlackhandClassVisitor extends ClassVisitor {

    public String mSuperName;
    public String mClazzName;
    public String[] mInterfaces;
    public ArrayList<BlackhandMethodInfo> mMethods;
    public int mVersion;
    public int mAccess;
    public String mSignature;
    public String mName;
    public HashMap<AnnotationNode, BlackhandMethodInfo> mProtocols;
    public AnnotationNode mActivityProtocolNode;
    public AnnotationNode mAnimActivityProtocolNode;


    public BlackhandClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mClazzName = name.replace("/",".");
        mName = name;
        mSuperName = superName.replace("/",".");
        mInterfaces = interfaces;
        mVersion = version;
        mAccess = access;
        mSignature = signature;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (Type.getDescriptor(ActivityProtocol.class).equals(desc)) {
            //协议
            mActivityProtocolNode = new AnnotationNode(desc);
            return mActivityProtocolNode;
        }else if(Type.getDescriptor(CustomAnimation.class).equals(desc)){
            mAnimActivityProtocolNode = new AnnotationNode(desc);
            return mAnimActivityProtocolNode;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        methodVisitor = new AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, desc) {


            public BlackhandMethodInfo methodInfo = new BlackhandMethodInfo();
            public String mProtocolName;
            public AnnotationNode mProtocolNode;

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                AnnotationNode an = new AnnotationNode(desc);
                if (Type.getDescriptor(MethodProtocol.class).equals(desc)) {
                    mProtocolNode = an;
                    return an;
                }
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                AnnotationNode an = new AnnotationNode(desc);
                if (Type.getDescriptor(MethodParam.class).equals(desc)
                        || Type.getDescriptor(MethodExtra.class).equals(desc)) {
                    MethodAnnotationsInfo methodAnnotationsInfo = new MethodAnnotationsInfo(parameter, desc, visible, an);
                    methodInfo.mMethodAnnotations.add(methodAnnotationsInfo);
                    return an;
                }
                return super.visitParameterAnnotation(parameter, desc, visible);

            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if(mMethods == null){
                    mMethods = new ArrayList<>();
                }
                mMethods.add(methodInfo);
                if(mProtocolNode != null){
//                    mProtocolName = (String)mProtocolNode.values.get(1);
                    if(mProtocols == null){
                        mProtocols = new HashMap<>();
                    }
                    mProtocols.put(mProtocolNode, methodInfo);
                }
            }

            @Override
            public void visitCode() {
                super.visitCode();

                List<Type> paramsTypeClass = new ArrayList();
                Type[] argsType = Type.getArgumentTypes(desc);
                for (Type type : argsType) {
                    paramsTypeClass.add(type);
                }
                methodInfo.mMethodName = name;
                methodInfo.mMethodParms = paramsTypeClass;
                methodInfo.mDesc = desc;

            }
        };
        return methodVisitor;

    }
}

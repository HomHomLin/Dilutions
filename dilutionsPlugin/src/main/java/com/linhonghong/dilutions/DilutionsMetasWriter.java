package com.linhonghong.dilutions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Linhh on 17/6/23.
 */

public class DilutionsMetasWriter implements Opcodes{
    public byte[] makeMetas(String pkg, HashMap<String, ArrayList<String>> metas){
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;

        cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, pkg, null, "java/lang/Object", null);


        fv = cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "map", "Ljava/util/HashMap;", "Ljava/util/HashMap<Ljava/lang/String;Ljava/util/ArrayList<Ljava/lang/String;>;>;", null);
        fv.visitEnd();


        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();


        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getMap", "()Ljava/util/HashMap;", "()Ljava/util/HashMap<Ljava/lang/String;Ljava/util/ArrayList<Ljava/lang/String;>;>;", null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, pkg, "map", "Ljava/util/HashMap;");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, pkg, "map", "Ljava/util/HashMap;");


        for(Map.Entry<String, ArrayList<String>> entrySet: metas.entrySet()){
            String key = entrySet.getKey();
            ArrayList<String> v = entrySet.getValue();
            //创建一个list
            mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            mv.visitVarInsn(Opcodes.ASTORE, 0);
            for(String s : v){
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitLdcInsn(s);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
                mv.visitInsn(Opcodes.POP);
            }
            mv.visitFieldInsn(Opcodes.GETSTATIC, pkg, "map", "Ljava/util/HashMap;");
            mv.visitLdcInsn(key);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(Opcodes.POP);
        }


        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }
}

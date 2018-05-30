package com.linhonghong.dilutions.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.linhonghong.dilutions.BlackhandClassVisitor
import com.linhonghong.dilutions.BlackhandMethodInfo
import com.linhonghong.dilutions.DilutionsMetasWriter
import com.linhonghong.dilutions.MethodAnnotationsInfo
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

/**
 * Created by Linhh on 17/5/31.
 */

public class PluginImpl extends Transform implements Plugin<Project> ,Opcodes{
    //Method metas
    private HashMap<String, ArrayList<String>> mMetas = new HashMap<>();

    //UI metas
    private HashMap<String, ArrayList<String>> mUIMetas = new HashMap<>();


    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension);
        android.registerTransform(this)
    }


    @Override
    public String getName() {
        return "Dilutions-plugin";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
//        return TransformManager.SCOPE_FULL_PROJECT;
//        QualifiedContent.Scope.PROJECT,
//        QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
//        QualifiedContent.Scope.SUB_PROJECTS,
//        QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
//        QualifiedContent.Scope.EXTERNAL_LIBRARIES
        return TransformManager.SCOPE_FULL_PROJECT
    }



    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
    }

    void processMetas( BlackhandClassVisitor cv){
        if(cv.mActivityProtocolNode != null){
            //UI跳转
            if(cv.mActivityProtocolNode.values != null
                    && cv.mActivityProtocolNode.values.size() > 1){
                String protocol_name = cv.mActivityProtocolNode.values.get(1);
                protocol_name = protocol_name.substring(1, protocol_name.length() - 1)
                if(!protocol_name.empty){
                    //动画添加
                    String enterAnim = "0";
                    String exitAnim = "0";
                    if(cv.mAnimActivityProtocolNode != null && cv.mAnimActivityProtocolNode.values != null
                            && cv.mAnimActivityProtocolNode.values.size() > 1){
                        try{
                            //进入动画设置
                            enterAnim = cv.mAnimActivityProtocolNode.values.get(1);
                        }catch (Exception e){

                        }

                        try{
                            //进入动画设置
                            exitAnim = cv.mAnimActivityProtocolNode.values.get(3);
                        }catch (Exception e){

                        }

                        println "找到针对"+protocol_name+"的UI动画Enter:" + enterAnim+ ",exit:"+exitAnim
                    }
                    String[] protocols = protocol_name.split(",")
                    for(String s : protocols){
                        ArrayList<String> strings = new ArrayList<>();
                        //添加对应的跳转类index=0
                        strings.add(cv.mClazzName)
                        //添加对应的enter动画index=1
                        strings.add(enterAnim)
                        //添加对应的exit动画index = 2
                        strings.add(exitAnim)
                        //将序列放入metas
                        mUIMetas.put(s.trim(), strings)
                    }
                }

                println "找到UI协议:" + protocol_name
            }
//            mUIMetas.put()
        }
        if(cv.mProtocols != null){
            //类名
            String clazz = cv.mClazzName;
            println clazz
            for (Map.Entry<AnnotationNode,BlackhandMethodInfo> entry : cv.mProtocols.entrySet()) {
                //协议
                String protocol_name = "";
                if(entry.key.values != null && entry.key.values.size() > 1){
                    protocol_name = entry.key.values.get(1);
                    println "找到协议:" + protocol_name
                }else{
                    println "没有找到协议,这有问题!"
                    continue
                }
                ArrayList<String> strings = new ArrayList<>();
                strings.add(clazz)
                //方法名
                String medhodname = entry.value.getMethodName()
                println medhodname;
                strings.add(medhodname)
                //方法参数类型
                String javaMethodParams = entry.value.getJavaMethodParams()
                println javaMethodParams
                strings.add(javaMethodParams)
                //参数以及其index
                for(MethodAnnotationsInfo info : entry.value.mMethodAnnotations){
                    strings.add(info.getIndex() + "=" +  info.getParamName())
                    println info.getIndex() + "=" + info.getParamName()
                }
                mMetas.put(protocol_name, strings)
            }
        }
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println '==================Dilutions transform start=================='
        //删除之前的输出
        if(outputProvider!=null)
            outputProvider.deleteAll()
        //遍历inputs里的TransformInput
        inputs.each { TransformInput input ->
            //遍历input里边的DirectoryInput
            input.directoryInputs.each {
                DirectoryInput directoryInput ->
                    //是否是目录
                    if (directoryInput.file.isDirectory()) {
                        //遍历目录
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                def filename = file.name;
                                def name = file.name
                                //这里进行我们的处理 TODO
                                if (name.endsWith(".class")) {
                                    //类处理
                                    ClassReader classReader = new ClassReader(file.bytes)
                                    ClassWriter classWriter = new ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
                                    BlackhandClassVisitor cv = new BlackhandClassVisitor(Opcodes.ASM5,classWriter)
                                    classReader.accept(cv, EXPAND_FRAMES)

                                    //处理类数据
//                                    println cv.mClazzName + ";" + cv.mSuperName
                                    processMetas(cv)
//                                    processMetas(cv, classWriter);
                                    //写入
                                    byte[] code = classWriter.toByteArray()
                                    FileOutputStream fos = new FileOutputStream(
                                            file.parentFile.absolutePath + File.separator + name)
                                    fos.write(code)
                                    fos.close()
//                                    println 'Dilutions-----> inject file:' + file.getAbsolutePath()
                                }
//                                println 'Dilutions-----> find file:' + file.getAbsolutePath()
                                //project.logger.
                        }
                    }
                    //处理完输入文件之后，要把输出给下一个任务
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }


            input.jarInputs.each { JarInput jarInput ->
                /**
                 * 重名名输出文件,因为可能同名,会覆盖
                 */
                def jarName = jarInput.name
                println "Dilutions jarName:" + jarName + "; "+ jarInput.file.absolutePath
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {

                    jarName = jarName.substring(0, jarName.length() - 4)
                }

                File tmpFile = null;
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    JarFile jarFile = new JarFile(jarInput.file);
                    Enumeration enumeration = jarFile.entries();
                    tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_anna.jar");
                    //避免上次的缓存被重复插入
                    if(tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
                    //用于保存
                    ArrayList<String> processorList = new ArrayList<>();
                    while (enumeration.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                        String entryName = jarEntry.getName();
                        ZipEntry zipEntry = new ZipEntry(entryName);

                        InputStream inputStream = jarFile.getInputStream(jarEntry);
                        //如果是inject文件就跳过
                        //anna插桩class
                        if (entryName.endsWith(".class")) {
                            //class文件处理
//                            println "entryName anna:" + entryName
                            jarOutputStream.putNextEntry(zipEntry);
                            ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                            ClassWriter classWriter = new ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
                            BlackhandClassVisitor cv = new BlackhandClassVisitor(Opcodes.ASM5,classWriter)
                            classReader.accept(cv, EXPAND_FRAMES)
                            //处理类数据
                            processMetas(cv)
//                            processMetas(cv, classWriter);

                            byte[] code = classWriter.toByteArray()
                            jarOutputStream.write(code);
                        } else if(entryName.contains("META-INF/services/javax.annotation.processing.Processor")){
                            if(!processorList.contains(entryName)){
//                                println "entryName no anna:" + entryName
                                processorList.add(entryName)
                                jarOutputStream.putNextEntry(zipEntry);
                                jarOutputStream.write(IOUtils.toByteArray(inputStream));
                            }else{
                                println "duplicate entry:" + entryName
                            }
                        }else {
//                            println "entryName no anna:" + entryName
                            jarOutputStream.putNextEntry(zipEntry);
                            jarOutputStream.write(IOUtils.toByteArray(inputStream));
                        }
                        jarOutputStream.closeEntry();
                    }
                    //结束
                    jarOutputStream.close();
                    jarFile.close();
//                    jarInput.file.delete();
//                    tmpFile.renameTo(jarInput.file);
                }
//                println 'Assassin-----> find Jar:' + jarInput.getFile().getAbsolutePath()

                //处理jar进行字节码注入处理 TODO

                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                println 'Blackhand-----> copy to Jar:' + dest.absolutePath
                if(tmpFile == null) {
                    FileUtils.copyFile(jarInput.file, dest)
                }else{
                    FileUtils.copyFile(tmpFile, dest)
                    tmpFile.delete()
                }
            }
        }

        //创建meta数据
        File meta_file = outputProvider.getContentLocation("dilutions_inject_metas", getOutputTypes(), getScopes(),
                Format.JAR);
        if(!meta_file.getParentFile().exists()){
            meta_file.getParentFile().mkdirs();
        }
        if(meta_file.exists()){
            meta_file.delete();
        }
        DilutionsMetasWriter metasWriter = new DilutionsMetasWriter();

//        JarFile jarFile = new JarFile(meta_file);
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(meta_file));
        ZipEntry addEntry = null;

        addEntry = new ZipEntry("com/linhonghong/dilutions/inject/support/DilutionsInjectMetas.class");
        jarOutputStream.putNextEntry(addEntry);
        jarOutputStream.write(metasWriter.makeMetas("com/linhonghong/dilutions/inject/support/DilutionsInjectMetas",mMetas));
        jarOutputStream.closeEntry();

        addEntry = new ZipEntry("com/linhonghong/dilutions/inject/support/DilutionsInjectUIMetas.class");
        jarOutputStream.putNextEntry(addEntry);
        jarOutputStream.write(metasWriter.makeMetas("com/linhonghong/dilutions/inject/support/DilutionsInjectUIMetas",mUIMetas));
        jarOutputStream.closeEntry();
        //结束
        jarOutputStream.close();
//        jarFile.close();

        println '==================Dilutions transform end=================='

    }
}
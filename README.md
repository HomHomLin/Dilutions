# Dilutions

Dilutions是一个专门用于模块间数据协议通信的解耦协议框架，提供高性能数据分析和通信功能,解耦多项目多模块间的数据通信,简化代码逻辑成本。

通过一段URI字符串就能实现所有操作。

这个框架已经在 美柚 稳定 ，2016 年就开始使用，美柚总用户突破1亿，日活接近千万，Dilutions框架经过亿万用户的测试，代码的稳定性是可以放心的。有需求或者bug可以提issues，我会尽快回复。

![](http://sc.seeyouyima.com/shopGuide/data/59647e039f684_1920_576.png?imageView2/2/w/800/h/600)

# 跨模块UI跳转

通过Dilutions实现跨模块间的UI跳转。

## 基本UI跳转

假设此时需要从模块1中的界面A跳转到模块2中的界面B，并且携带一些数据，由于模块1和模块2之间互不依赖，想要跨模块打开某个界面是比较困难的。

Dilutions提供了跨模块间的UI跳转能力，通过定义一串共同的URI协议即可实现界面A到界面B的跳转。

假设我们约定这串跳转协议URI为:

```java
String uri = "dilutions:///ui/atob"
```

那么，这时只要在界面B中加上注解：

```java
@ActivityProtocol("/ui/atob")
public class ActivityB extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
```

界面A调用如下语句即可实现UI跳转:

```java
    Dilutions.create().formatProtocolService(uri);
```

## 携带数据

如果这时候界面B需要接收一些参数，那么界面A如何传递呢？

我们假设界面B需要的参数如下：

```java
int user_id;
String user_name;
```

那么界面B只需要将界面代码改为：

```java
@ActivityProtocol("/ui/atob")
public class ActivityB extends AppCompatActivity {
    @ActivityProtocolExtra("user_id")
    int user_id;

    @ActivityProtocolExtra("user_name")
    String user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dilutions.create().register(this);//注意，一定要注册dilutions

        Log.i(user_id + user_name);//使用传递过来的数据
    }
}
```

界面A通过调用如下代码即可传递参数并且实现跳转：

```java
    String uri = "dilutions:///ui/atob"
    HashMap<String,Object> map = new HashMap<>();
    map.put("user_id", 222);
    map.put("user_name", "二红");
    Dilutions.create().formatProtocolService(uri, map, null);
```

## 携带对象数据

有的时候，我们传递的不一定是基础类型，而是对象，Dilutions也可以帮你完成传递

```java
@ActivityProtocol("/ui/atob")
public class ActivityB extends AppCompatActivity {
    @ActivityProtocolExtra("test_object")
    TestObject test_object;
}
```

界面A通过调用如下代码即可传递参数并且实现跳转：

```java
    String uri = "dilutions:///ui/atob"
    HashMap<String,Object> map = new HashMap<>();
    map.put("test_object", new TestObject);
    Dilutions.create().formatProtocolService(uri, map, null);
```

而界面B中可以直接拿到那个对象进行使用，但是需要注意的是传递的对象必须序列化。

## 转场动画

有时候需要动画转场来跳转界面，Dilutions也支持Activity的动画转场

只需要在对应的Activity加上注解即可

```java
@ActivityProtocol({"/test","/test2"})
@CustomAnimation(enter = R.anim.enter, exit = R.anim.exit)
public class MainActivity extends AppCompatActivity {
}
```

CustomAnimation注解中的enter表示进入Activity的动画，exit为退出

# 跨模块方法调用

我们上面学会了跨模块的UI跳转，下面将学到如何通过Dilutions进行跨模块的方法调用。

假设模块1想调用模块2的一个方法，模块1和模块2不互相依赖，你该怎么做呢？

再比如现在有方法A和方法B，想通过服务器决定来调用哪一个，怎么做比较好呢？

Dilutions帮你解决了这个问题，使用方式和UI跳转差不多，你只需要URI协议字符串即可。

## 基础的跨模块调用方法

假设模块2中存在一个原生方法，这个方法之前是被模块2内的其他类直接使用的，现在需要支持跨模块特性。

首先模块2的方法需要加上注解：

```java
@MethodProtocol("/method")
public void method(){
     Log.d("test","method had being called.");
}
```

这个方法可以在任何一个类中，任意地方，只需要注解。

接下来，你应该从UI跳转方法中学到了如何识别协议URI，没错，这个方法中的"/method"就是协议，因此协议应该长这样：

```java
String uri = "dilutions:///method";
```

那么调用方依然是

```java
Dilutions.create().formatProtocolService(uri);
```

这样就完成了跨模块的方法调用。

所以你可以看到入口是不变的，一个方法可以通过URI协议的不同，正确的调用实现方。

## 携带参数

那么这时候你肯定想到了，我的方法肯定不可能都是无参啊，Dilutions可以支持带参调用吗？

答案是肯定的，仍然是通过注解。

我们假设有参方法method2如下：

```java
public void method2(String username, int userid, boolean open){
     Log.d("test","method had being called." + username + userid);
}
```

那么需要添加注解，改造成如下代码：

```java
@MethodProtocol("/method2")
public void method2(@MethodParam("username")String username, @MethodParam("userid")int userid, @MethodParam("open")boolean open){
     Log.d("test","method had being called." + username + userid);
}
```

调用方只需要跟带数据跳转UI的操作方式一致就可以：

```java
    String uri = "dilutions:///method2"
    HashMap<String,Object> map = new HashMap<>();
    map.put("user_id", 222);
    map.put("user_name", "二红");
    map.put("open", true);
    Dilutions.create().formatProtocolService(uri, map, null);
```

如果传递的数据不存在，比如没有传递open这个数据，那么对应的实现方法仍然会被执行，但是对应的入参会以默认值传入。

## 携带对象数据

跟跳转UI一样，Dilutions也可以携带对象数据跨模块调用方法。

```java
@MethodProtocol("/method2")
public void method2(@MethodParam("username")TestObject username){
     Log.d("test","method had being called.");
}
```

## 方法实现方相关

Dilutions对实现方的方法会进行自动映射，实现方不一定需要全部将入参标注参数，并且对注解顺序没有任何要求，比如：

```java
@MethodProtocol("/method2")
public void method2(@MethodParam("username")String username, int userid, @MethodParam("open")boolean open){
     Log.d("test","method had being called." + username + userid);
}
```

## 方法返回值

那么你肯定还会提到，有的方法还有返回值，那么怎么办呢？

Dilutions同样解决了这个问题。

将方法method2改为：

```java
@MethodProtocol("/method2")
public int method2(@MethodParam("username")String username, int userid, @MethodParam("open")boolean open){
     Log.d("test","method had being called." + username + userid);
     return 0;
}
```

调用方改为

```java
Dilutions.create().formatProtocolServiceWithCallback(uri,new DilutionsCallBack(){
    public void onDilutions(DilutionsData data){
        Object result = data.getResult();
        //result即为方法调用返回值结果
    }
});
```

## URI协议

在Dilutions中，我们知道所有的跨模块操作都是基于协议，因此，我们通过一串URI字符串即可实现跨模块的数据交互。

这个URI协议可以是从服务器下发的，也可以是客户端直接写好的，通过这个方式可以实现动态的方法调用和界面跳转。

在Dilutions中，协议的格式如下：

```xml
dilutions:///circles/group?params=e2dyb3VwSUQ6Myx0ZXN0OiLmnpflro/lvJgifQ==
```

其中dilutions:// 是协议头，这个是可以通过Dilutions自定义的，你可以给不同的app、业务设置不同的协议头，用来区分。

其中/circles/group这种，你已经知道了，他就是主要协议path，只有实现方实现了才会响应。

后面的params其实是固定样式，params=后面跟的是协议的数据参数，这个数据参数是base64过的json。

所以如果要实现动态跳转或者动态方法调用，服务器下发协议字符串需要采用这个逻辑构造。

在客户端中，Dilutions提供了一系列的工具类来帮助使用者生成，正常来说，我们只需要使用Dilutions.formatService相关方法即可，但是需求总是会变的，所以接下来会展示如何生成一个Dilutions的URI协议。

### 客户端生成URI协议

```java
String uri = DilutionsUriBuilder.buildUri("dilutions://", "/testmap","{ \"path\":\"bi_information\", \"tt\" : {\"action\":1,\"floor\":2}}");
Dilutions.create().formatProtocolService(uri);
```

以上代码片段是生成生成一个URI协议，然后再执行它。

你可以通过DilutionsUriBuilder这个类进行协议的生成和解析操作。

### 拦截协议

有的时候你可能需要拦截部分协议，在它们执行前进行额外的操作，那么拦截器是你的不二之选。

```java
Dilutions.create().formatProtocolServiceWithInterceptor(uri, new DilutionsInterceptor(){
    public boolean interceptor(DilutionsData data){
       return false;
    }
})
```

在拦截器的DilutionsData回调对象中存在很多获得协议信息的方法，比如执行的intent等，你可以对其进行修改，你甚至可以在里面重新定向到另一个协议实现，通过更改boolean返回值来决定（true=拦截，不继续执行原本的协议，false=继续执行）。

### 添加协议头

你可以通过动态添加协议头来决定你当前应用需要支持哪些协议。

```java
Dilutions.create().getAppMap().add("dilutions2");
```

## 代理跳转UI

上面已经介绍过使用URI协议进行跳转UI，但实际上在Dilutions中，你还可以通过动态代理的方式进行跳转。

首先需要编写代理接口

```java
public interface DebugService {

    @ProtocolPath("/test")
    void renderPage(@ExtraParam("test") String test, @ExtraParam("id") Object obj);
}
```

其中ProtocolPath内的是协议的path，方法名随意，ExtraParam注解对应的是参数名，后面跟类型。

编写完代理接口后，通过调用代理接口即可实现协议跳转执行。

```java
Dilutions.create().formatProtocolService(DebugService.class).renderPage(参数);
```

PS：当前版本动态代理只能跳转UI，后续会实现跳转方法实现。

## 获取协议数据

### 注解获取

当你发起了一个协议打开一个UI的时候，你需要获得传递过来的协议数据，通过Dilutions注解可以在Activity或者Fragment中获得协议数据。

在Activity中：

```java
    /**
     * 读取test参数
     */
    @ActivityProtocolExtra("test")
    TestObj st;

    /**
     * 读取query参数
     */
    @ActivityProtocolExtra("query")
    int query;

    @ActivityProtocolExtra("groupID")
    int groupID;

```


在Fragment中：

```java
    /**
     * 读取test参数
     */
    @FragmentArg("test")
    TestObj st;

    /**
     * 读取query参数
     */
    @FragmentArg("query")
    int query;

    @FragmentArg("groupID")
    int groupID;

```


最后需要在对应的Activity或者Fragment的onCreate()方法中注册

```java
Dilutions.create().register(this);
```


注册完毕后，这些数据参数就可以使用了。

PS：Dilutions直接任意数据对象传递。

### 原始获取

你可以不通过注解方式获取，你可以通过getIntent来获取注解，比如上述的数据通过下面的方式也可以获取到：

```java
int query = getIntent().getIntExtra("query",0);
```

### 额外参数获取

有时候你可能还想获得传递过来的完整协议，或者其他信息来处理一些业务需求，Dilutions定义了一些参数名，你可以通过注解方式也可以通过原始方式获得对应的数据。

```java
class DilutionsInstrument{
    //协议目标class类
    public static final String URI_CALL_CLASS = "uri-call-clazz";
    //协议的path
    public static final String URI_CALL_PATH = "uri-call-path";
    //协议的参数
    public static final String URI_CALL_PARAM = "uri-call-param";
    //完整协议
    public static final String URI_CALL_ALL = "uri-call-all";
    //如何跳转的，是代理还是协议
    public static final String URI_FROM = "uri-from";
}
```

## 初始化

Dilutions需要初始化才能够被使用，只需要一次即可。

```java
Dilutions.init(Context);
```

## gradle

当前最新版本1.0.8


在主工程最外层配置gradle ：classpath 'linhonghong.lib:dilutions-compiler:1.0.6'

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.0'
        //添加这个
        classpath 'linhonghong.lib:dilutions-compiler:1.0.8'
    }
}
```

在需要dilutions的地方添加：

```groovy
	compile 'linhonghong.lib:dilutions:1.0.8'
```

Dilutions依赖阿里巴巴fastjson的json解析，因此需要在你的工程中依赖fastjson

```groovy
	compile 'com.alibaba:fastjson:1.1.68.android'
```

主工程apply插件

```groovy
apply plugin: 'dilutions'
```

Dilutions在Gradle2.x以及以上版本测试通过。

## 混淆

```xml
-keep public class com.linhonghong.dilutions.inject.support.DilutionsInjectUIMetas
-keep public class com.linhonghong.dilutions.inject.support.DilutionsInjectMeta
```

## Developed By

 * Linhonghong - <linhh90@163.com>

## License
Copyright 2016 LinHongHong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

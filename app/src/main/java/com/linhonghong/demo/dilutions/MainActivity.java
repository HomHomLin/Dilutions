package com.linhonghong.demo.dilutions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.linhonghong.dilutions.Dilutions;
import com.linhonghong.dilutions.annotations.ActivityProtocol;
import com.linhonghong.dilutions.annotations.ActivityProtocolExtra;
import com.linhonghong.dilutions.annotations.CustomAnimation;
import com.linhonghong.dilutions.utils.DilutionsUriBuilder;

import java.lang.reflect.*;
import java.util.HashMap;

/**
 * 演示UI协议跳转
 * 表示该Activity支持"/test","/test2"这两个协议
 */
@ActivityProtocol({"/test","/test2"})
@CustomAnimation(enter = R.anim.enter, exit = R.anim.exit)
public class MainActivity extends AppCompatActivity {

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

//    String test = "dilutions:///test?params=e2dyb3VwSUQ6Mn0=";
    String test = "dilutions:///circles/group?params=e2dyb3VwSUQ6Myx0ZXN0OiLmnpflro/lvJgifQ==";
    //dilutions:///circles/group?params={groupID:0}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dilutions.init(this);
        //Activity注册
        Dilutions.create().register(this);

        //直接使用st参数
//        Log.i("test",st);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //跳转方法
                if(test.equals("dilutions:///circles/group?params=e2dyb3VwSUQ6Myx0ZXN0OiLmnpflro/lvJgifQ==")){
                    test = "dilutions:///circles/group?params=e2dyb3VwSUQ6Mn0=";
                }else{
                    test = "dilutions:///circles/group?params=e2dyb3VwSUQ6Myx0ZXN0OiLmnpflro/lvJgifQ==";
                }
                String s = DilutionsUriBuilder.buildUri("dilutions://", "/testmap","{ \"path\":\"bi_information\", \"tt\" : {\"action\":1,\"floor\":2}}");
                Dilutions.create().formatProtocolService(s);
//                formatProtocolService(Test.class).renderPage();
//                Dilutions.create().formatProtocolService(DebugService.class).renderPage();
//                Dilutions.create().formatProtocolService(test);
            }
        });
        findViewById(R.id.btn_test1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //协议跳转
                Bundle bundle = new Bundle();
//                bundle.putString("test",new TestObj());
                bundle.putString("name","linhonghong");
                HashMap<String,Object> extraMap = new HashMap<>();
                extraMap.put("mycallback", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("testmethodobj", "回调完成");
                    }
                });
                Log.d("testmethodobj", "协议开始");
                Dilutions.create().formatProtocolServiceWithMap("dilutions:///obj?params=eyJ0dCI6eyJ0IjoieHh4c3NzIiwieCI6MTIyM319", null, extraMap);
//                Dilutions.create().formatProtocolService("dilutions","test",bundle)
//                Dilutions.create().setDilutionsPathInterceptor("/test", new DilutionsPathInterceptor() {
//                    @Override
//                    public boolean interceptor(DilutionsData data) {
//                        return false;
//                    }
//                });
                HashMap<String,Object> map = new HashMap<>();
                map.put("query", 222);
                TestObj r = new TestObj();
                r.t = "tttt";
                map.put("test", r);
                Dilutions.create().formatProtocolService("dilutions:///test?params=e2dyb3VwSUQ6Mn0=", map, null);
//                Dilutions.create().formatProtocolService("dilutions:///finish");
//                String te = "dilutions:///doubles?params=eyJkb3VibGVzIjoyLjIyfQ==";
//                Dilutions.create().formatProtocolServiceWithCallback(te, new DilutionsCallBack() {
//                    @Override
//                    public void onDilutions(DilutionsData data) {
//                        Log.i("res", data.getResult().toString());
//                    }
//                });
            }
        });

    }
}

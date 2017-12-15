package com.gi2t.face.detect.activity;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        
        Log.e("dengying", "MyApplication onCreate");
    }
    
    /**
     * 获取全局上下文*/
    public static Context getContext() {
        return context;
    }
}
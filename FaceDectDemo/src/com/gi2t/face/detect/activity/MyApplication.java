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
     * ��ȡȫ��������*/
    public static Context getContext() {
        return context;
    }
}
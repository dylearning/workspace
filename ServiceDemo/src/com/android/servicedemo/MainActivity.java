package com.android.servicedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		Intent i = new Intent(this, AppService.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startService(i);
    }
}

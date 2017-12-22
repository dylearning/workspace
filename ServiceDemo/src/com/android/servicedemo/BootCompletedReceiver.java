package com.android.servicedemo;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	
	private final static String TAG = "dengying";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			
			Log.e(TAG, "BootCompletedReceiver");
			
			Intent i = new Intent(context, AppService.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(i);
		}
	}
}
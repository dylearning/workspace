package com.android.servicedemo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class AppService extends Service {

	private final static String TAG = "dengying";

	private final static String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	
	private boolean isServiceStarted = false;
	
	private SmsReceiver mSmsReceiver = new SmsReceiver();
	
	private Intent startIntent = null;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "AppService onCreate");

		//registerBoradcastReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "AppService onStartCommand");
		
		//onStart(intent, startId);
		//return super.onStartCommand(intent, flags, startId);
		
		//START_STICKY�����service���̱�kill��������service��״̬Ϊ��ʼ״̬�������������͵�intent����
		//���ϵͳ�᳢�����´���service�����ڷ���״̬Ϊ��ʼ״̬��
		//���Դ��������һ�������onStartCommand(Intent,int,int)������
		//����ڴ��ڼ�û���κ�����������ݵ�service����ô����Intent��Ϊnull
		  
		return START_STICKY;   
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		startIntent = intent;
		
		Log.e(TAG, "AppService, onStart");
		
		if(!isServiceStarted){
			isServiceStarted = true;
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "AppService, onDestroy");
		
		isServiceStarted = false;
		
		//�����ٵ�ʱ�����¿���
		if (startIntent != null) {
			startService(startIntent);
		}
		//unregisterBoradcastReceiver();
	}

	public void registerBoradcastReceiver() {
		IntentFilter mLocalFilter = new IntentFilter();	
		mLocalFilter.addAction(SMS_ACTION);
		registerReceiver(mSmsReceiver, mLocalFilter);
	}

	public void unregisterBoradcastReceiver() {
		if (mSmsReceiver != null) {
			unregisterReceiver(mSmsReceiver);
		}
	}
	
	private class SmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction().toString();
			
			Log.e(TAG, "SmsReceiver action=" + action );
			
			if (action.equals(SMS_ACTION)) {
				
			}
		}
	}
}

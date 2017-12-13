package org.jan.nio.exapmle;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	private static final String TAG = "dengying";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		MyClientThread myClientThread = new MyClientThread();
		myClientThread.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}

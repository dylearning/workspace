package com.gi2t.face.detect.activity;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.gi2t.face.detect.camera.CameraInterface;
import com.gi2t.face.detect.camera.preview.CameraSurfaceView;
import com.gi2t.face.detect.mode.GoogleFaceDetect;
import com.gi2t.face.detect.R;
import com.gi2t.face.detect.ui.FaceView;
import com.gi2t.face.detect.util.DisplayUtil;
import com.gi2t.face.detect.util.EventUtil;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ai.aip.demo.FaceAdd;
import com.baidu.ai.aip.demo.FaceDelete;
import com.baidu.ai.aip.demo.FaceIdentify;
import com.baidu.ai.aip.demo.FaceMatch;
import com.baidu.ai.aip.demo.FaceVerify;

public class CameraActivity extends Activity{

	private static final String TAG = "yanzi";
	private CameraSurfaceView surfaceView = null;
	private ImageButton shutterBtn;
	private ImageButton switchBtn;
	
	private ImageView img_face_1;
	private ImageView img_face_2;
	private TextView txt_score;
	private TextView txt_usrinfo;
	private boolean isShowImg1 = true;
	
	private FaceView faceView;
	float previewRate = -1f;
	private MainHandler mMainHandler = null;
	private GoogleFaceDetect googleFaceDetect = null;
	
	boolean isTakePicture = false;
	private String preSaveFileName="";
	private String curSaveFileName="";
	
	private CameraInterface mCameraInterface;
	
	boolean isGoogleFaceDetect = false;
	
	private FaceDeleteThread mFaceDeleteThread;
	private FaceVerifyThread mFaceVerifyThread;
	private FaceIdentifyThread mFaceIdentifyThread;
	private FaceAddThread mFaceAddThread;
	private OpenDoorThread mOpenDoorThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		mMainHandler = new MainHandler();
		googleFaceDetect = new GoogleFaceDetect(getApplicationContext(), mMainHandler);//dengying

		//shutterBtn.setOnClickListener(new BtnListeners());
		switchBtn.setOnClickListener(new BtnListeners());
		mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
		
		mCameraInterface = CameraInterface.getInstance(getApplicationContext(), mMainHandler);
		
		/*Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isTakePicture = true;
				Log.e("dengying","isTakePicture="+isTakePicture);
			}
		}, 500, 15*1000);*/	// 0.5秒之后，每隔10秒做一次run()操作

		
		/* 人脸数据库删除*/
		/*if(mFaceDeleteThread == null){
			mFaceDeleteThread = new FaceDeleteThread();
			mFaceDeleteThread.start();
		}*/
		
		mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	private void initUI(){
		surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
		//shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
		switchBtn = (ImageButton)findViewById(R.id.btn_switch);
		faceView = (FaceView)findViewById(R.id.face_view);
		
		img_face_1 = (ImageView)findViewById(R.id.img_face_1);
		img_face_2 = (ImageView)findViewById(R.id.img_face_2);
		txt_score = (TextView)findViewById(R.id.txt_score);
		txt_usrinfo = (TextView)findViewById(R.id.txt_usrinfo);	
	}
	private void initViewParams(){
		LayoutParams params = surfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
		surfaceView.setLayoutParams(params);
	}

	private class BtnListeners implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			/*case R.id.btn_shutter:
				takePicture();
				break;*/
			case R.id.btn_switch:
				switchCamera();
				break;
			default:break;
			}
		}

	}
	private  class MainHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what){
			case EventUtil.UPDATE_FACE_RECT:
				
				Face[] faces = (Face[]) msg.obj;
				faceView.setFaces(faces);
				
				if(faces != null && faces.length >0 && isTakePicture){
					takePicture();
					//shutterBtn.performClick();
					Toast.makeText(CameraActivity.this, "检测到人脸，拍照！", Toast.LENGTH_SHORT).show();
					
					isTakePicture = false;
					Log.e("dengying","takePicture");
				}
				
				break;
			case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
				startGoogleFaceDetect();
				break;
				
			case EventUtil.TAKE_PICTURE_FILENAME:
				curSaveFileName = (String) msg.obj;
				
				Log.e("dengying","TAKE_PICTURE_FILENAME curSaveFileName="+curSaveFileName);
				
				/*BitmapFactory.Options options = new BitmapFactory.Options();
		        options.inSampleSize = 10;   //width，hight设为原来的十分一
		        options.inPurgeable = true;
		        options.inInputShareable = true;
		        options.inJustDecodeBounds = false;
		        options.inPreferredConfig = Bitmap.Config.RGB_565;//避免出现内存溢出的情况，进行相应的属性设置。
		        options.inDither = true;
		        
				Bitmap bitmap = BitmapFactory.decodeFile(curSaveFileName,options);*/
				
				Bitmap bitmap = BitmapFactory.decodeFile(curSaveFileName);
				
				Log.e("dengying","TAKE_PICTURE_FILENAME bitmap="+bitmap.toString());
				if (isShowImg1) {
					img_face_1.setImageBitmap(bitmap);
					isShowImg1 = false;
				} else {
					img_face_2.setImageBitmap(bitmap);
					isShowImg1 = true;
				}
				
				isTakePicture = false;
				
				/* 人脸认证 */		
				/*if(mFaceVerifyThread == null){
					Log.e("dengying","mFaceVerifyThread == null");
					mFaceVerifyThread = new FaceVerifyThread();
					mFaceVerifyThread.start();
				}*/
				
				/* 人脸识别  */
				if(mFaceIdentifyThread == null){
					mFaceIdentifyThread = new FaceIdentifyThread();
					mFaceIdentifyThread.start();
				}
				
				/* 人脸注册 */
				//new FaceAddThread().start();
				
		        /* 人脸对比 */	
				//new FaceMatchThread().start();
				
				preSaveFileName = curSaveFileName;
				break;	
						
			case EventUtil.BAIDU_FACE_DELETE:
				
				if (mFaceDeleteThread != null) {  
					mFaceDeleteThread.interrupt();  
					mFaceDeleteThread = null;  
			     }  
				
				boolean delete_result =  (Boolean)msg.obj;
				String s_delete_result = "";
				
				if(delete_result){
					s_delete_result="人脸数据库清除，初始化成功！";
				}else{
					s_delete_result="人脸数据库删除失败！";
				}
				Toast.makeText(CameraActivity.this, s_delete_result, Toast.LENGTH_SHORT).show();
				
				mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
				//isTakePicture = true;
				
				break;					
				
			case EventUtil.BAIDU_FACE_VERIFY:
				if (mFaceVerifyThread != null) {  
					mFaceVerifyThread.interrupt();  
					mFaceVerifyThread = null;  
			     } 
				
				int verify_result =  (int)Double.parseDouble((String) msg.obj);
				String s_verify_result = "";
				
				if(verify_result>=80){
					s_verify_result="人脸验证成功，开始开锁！";
					
					//开门
					mOpenDoorThread = new OpenDoorThread();
			        mOpenDoorThread.start();
					
					//txt_score.setText("Score:"+verify_result);
				}else{
					s_verify_result="人脸验证,没有注册，现在开始注册。";
					
					/* 人脸注册 */
					if(mFaceAddThread == null){
						mFaceAddThread = new FaceAddThread();
					}
					mFaceAddThread.start();
				}
				
				Toast.makeText(CameraActivity.this, s_verify_result, Toast.LENGTH_SHORT).show();
				
				break;					
				
			case EventUtil.BAIDU_FACE_IDENTIFY:
				if (mFaceIdentifyThread != null) {  
					mFaceIdentifyThread.interrupt();  
					mFaceIdentifyThread = null;  
			     } 			
				
				String s_identify_result =  (String) msg.obj;
				
				String error_msg="";
				
				String group_id="";
				String uid="";
				String user_info="";
				String scores="";
				
				String s_identify_message = "";
				
				try {
					JSONObject root = new JSONObject(s_identify_result);
					error_msg = root.getString("error_msg");
					
					txt_usrinfo.setText(error_msg);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				try {
					JSONObject root = new JSONObject(s_identify_result);
	
			        JSONArray array = root.getJSONArray("result");

			        for (int i = 0; i < array.length(); i++) {
			            JSONObject result = array.getJSONObject(i);
			            
						group_id = result.getString("group_id");
						uid = result.getString("uid");
						user_info = result.getString("user_info");
						scores = result.getString("scores");
						
						int length =scores.length();
						scores = scores.substring(1,(length-1));
						
						Log.e("dengying","BAIDU_FACE_IDENTIFY group_id="+group_id+","+",uid="+uid+",user_info="+user_info+",scores="+scores);
						
						txt_usrinfo.setText(user_info+"\nuid="+uid+"\nscores="+scores);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("dengying","BAIDU_FACE_IDENTIFY e="+e.toString());
				}
				
				if(error_msg.equals("no user in group")){
					s_identify_message="人脸没有注册，现在开始注册！";

					//人脸注册 
					if(mFaceAddThread == null){
						mFaceAddThread = new FaceAddThread();
					}
					mFaceAddThread.start();
					
				}else{
					s_identify_message="人脸验证成功，开始开锁！";
					
					
					//开门
					mOpenDoorThread = new OpenDoorThread();
			        mOpenDoorThread.start();
			        
			        int score =  (int)Double.parseDouble(scores);
					txt_score.setText("Score:"+score);
				}
				
				Toast.makeText(CameraActivity.this, s_identify_message, Toast.LENGTH_SHORT).show();
				break;
				
			case EventUtil.BAIDU_FACE_ADD:
				
				if (mFaceAddThread != null) {  
					mFaceAddThread.interrupt();  
					mFaceAddThread = null;  
			     } 
				
				boolean add_result =  (Boolean) msg.obj;
				String s_add_result = "";
				
				if(add_result){
					s_add_result="人脸注册成功！";
				}else{
					s_add_result="人脸注册失败！";
				}
				
				Toast.makeText(CameraActivity.this, s_add_result, Toast.LENGTH_SHORT).show();
				
				mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 3000);
				//isTakePicture =  true;
				
				break;										
				
			case EventUtil.BAIDU_FACE_WAIT:

				Toast.makeText(CameraActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
				
				break;					
				
			case EventUtil.BAIDU_FACE_MATCH:
				int scoreResult =  (int)Double.parseDouble((String) msg.obj);
				
				Log.e("dengying","UPDATE_BAIDU_SCORE scoreResult="+scoreResult);
				
				Toast.makeText(CameraActivity.this, "人脸对比，分数："+scoreResult, Toast.LENGTH_SHORT).show();
				txt_score.setText("Score:"+scoreResult);
				
				break;	
				
			case EventUtil.FACE_TAKE_PICTURE:
				isTakePicture = true;
				break;					
				
			case EventUtil.OPEN_DOOR:
				if (mOpenDoorThread != null) {  
					mOpenDoorThread.interrupt();  
					mOpenDoorThread = null;  
			     } 
				
				String open_door_ret =  (String) msg.obj;
				
				if(open_door_ret.equals("OK")){
					Toast.makeText(CameraActivity.this, "开门成功，开始下一个验证", Toast.LENGTH_SHORT).show();
					
					mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 5000);
					//isTakePicture = true;
				}else{
					Toast.makeText(CameraActivity.this, "开门失败，请检查设备", Toast.LENGTH_SHORT).show();
				}
				
				break;				
			}
			super.handleMessage(msg);
		}

	}

	private void takePicture(){
		mCameraInterface.doTakePicture();//dengying
		mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
	}
	private void switchCamera(){
		if (isGoogleFaceDetect) {
			stopGoogleFaceDetect();
		}
		
		int newId = (mCameraInterface.getCameraId() + 1)%2;
		mCameraInterface.doStopCamera();
		mCameraInterface.doOpenCamera(null, newId);
		mCameraInterface.doStartPreview(surfaceView.getSurfaceHolder(), previewRate);
		mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
//		startGoogleFaceDetect();
	}
	
	private void startGoogleFaceDetect(){
		Camera.Parameters params = mCameraInterface.getCameraParams();
		if(params != null && params.getMaxNumDetectedFaces() > 0){
			if(faceView != null){
				faceView.clearFaces();
				faceView.setVisibility(View.VISIBLE);
			}
			mCameraInterface.getCameraDevice().setFaceDetectionListener(googleFaceDetect);
			mCameraInterface.getCameraDevice().startFaceDetection();
		}
		
		isGoogleFaceDetect = true;
	}
	
	private void stopGoogleFaceDetect(){
		Camera.Parameters params = mCameraInterface.getCameraParams();
		if(params != null && params.getMaxNumDetectedFaces() > 0){
			mCameraInterface.getCameraDevice().setFaceDetectionListener(null);
			mCameraInterface.getCameraDevice().stopFaceDetection();
			faceView.clearFaces();
		}
		
		isGoogleFaceDetect = false;
	}

	
	@Override
	protected void onDestroy() {
		Log.e("dengying","onDestroy");
		
		mMainHandler.removeMessages(EventUtil.UPDATE_FACE_RECT);
		mMainHandler.removeMessages(EventUtil.CAMERA_HAS_STARTED_PREVIEW);
		mMainHandler.removeMessages(EventUtil.TAKE_PICTURE_FILENAME);
		mMainHandler.removeMessages(EventUtil.BAIDU_FACE_ADD);
		mMainHandler.removeMessages(EventUtil.BAIDU_FACE_VERIFY);
		mMainHandler.removeMessages(EventUtil.BAIDU_FACE_DELETE);
		mMainHandler.removeMessages(EventUtil.BAIDU_FACE_MATCH);
		mMainHandler.removeMessages(EventUtil.BAIDU_FACE_WAIT);
		mMainHandler.removeMessages(EventUtil.OPEN_DOOR);
		mMainHandler.removeMessages(EventUtil.FACE_TAKE_PICTURE);
		
		if (mFaceDeleteThread != null) {  
			mFaceDeleteThread.interrupt();  
			mFaceDeleteThread = null;  
	     }  
		
		if (mFaceAddThread != null) {  
			mFaceAddThread.interrupt();  
			mFaceAddThread = null;  
	     }  
		
		if (mFaceVerifyThread != null) {  
			mFaceVerifyThread.interrupt();  
			mFaceVerifyThread = null;  
	     }
		
		if (mOpenDoorThread != null) {  
			mOpenDoorThread.interrupt();  
			mOpenDoorThread = null;  
	     }
		
		stopGoogleFaceDetect();
		mCameraInterface.doStopCamera();
		
		android.os.Process.killProcess(android.os.Process.myPid()); 
		
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	
	/* 人脸数据库删除*/
	class FaceDeleteThread extends Thread {

		public void run() {
			boolean ret = FaceDelete.delete();

			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.BAIDU_FACE_DELETE;
			m.obj = ret;
			m.sendToTarget();
		}
	}
	
	/* 人脸注册*/
	class FaceAddThread extends Thread {

		public void run() {
			boolean add_result = FaceAdd.add(curSaveFileName);
			
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.BAIDU_FACE_ADD;
			m.obj = add_result;
			m.sendToTarget();
		}
	}
	
	/* 人脸验证*/
	class FaceVerifyThread extends Thread {

		public void run() {
			Message mWait = mMainHandler.obtainMessage();
			mWait.what = EventUtil.BAIDU_FACE_WAIT;
			mWait.obj = "人脸开始认证，请稍后！";
			mWait.sendToTarget();
			
			String verify_result = FaceVerify.verify(curSaveFileName);
			
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.BAIDU_FACE_VERIFY;
			m.obj = verify_result;
			m.sendToTarget();
		}
	}
	
	
	/* 人脸识别*/
	class FaceIdentifyThread extends Thread {

		public void run() {
			Message mWait = mMainHandler.obtainMessage();
			mWait.what = EventUtil.BAIDU_FACE_WAIT;
			mWait.obj = "人脸开始识别，请稍后！";
			mWait.sendToTarget();
			
			String identify_result = FaceIdentify.identify(curSaveFileName);
			
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.BAIDU_FACE_IDENTIFY;
			m.obj = identify_result;
			m.sendToTarget();
		}
	}
	
	/* 人脸对比*/
	class FaceMatchThread extends Thread {

		public void run() {
		    if(!preSaveFileName.equals("")){		    	
		    	
				Message mWait = mMainHandler.obtainMessage();
				mWait.what = EventUtil.BAIDU_FACE_WAIT;
				mWait.obj = "人脸数据正在对比，请稍后！";
				mWait.sendToTarget();
		    	
				String scoreResult = FaceMatch.match(preSaveFileName, curSaveFileName);
				
				Log.e("dengying","TAKE_PICTURE_FILENAME baiduResult="+scoreResult);
				
				Message m = mMainHandler.obtainMessage();
				m.what = EventUtil.BAIDU_FACE_MATCH;
				m.obj = scoreResult;
				m.sendToTarget();
			}
		}
	}
	
	class OpenDoorThread extends Thread {
		public void run() {
			Log.i("dengying","OpenDoorThread run");
			openDoor();
		}
	}
	
	private void openDoor(){
		Log.i("dengying","openDoor");
		
        try {
    		//测试服务器所在的项目URL
            String SERVER_URL = "http://192.168.14.149:8111/tdface/door";
            HttpPost postRequest = new HttpPost(SERVER_URL);
            
            //构造请求的json串
            JSONObject para = new JSONObject();
			para.put("door", "1");
	        StringEntity entity = new StringEntity(para.toString(), "utf-8");
	        HttpClient client = new DefaultHttpClient();
	        postRequest.setEntity(entity);
	        HttpResponse response = client.execute(postRequest);
	        
	        int resCode=response.getStatusLine().getStatusCode();
	        String result = EntityUtils.toString(response.getEntity(), "utf-8");
	        
	        Log.i("dengying", "openDoor,resCode = " + resCode); //获取响应码  
	        Log.i("dengying", "openDoor,result = " + result);//获取服务器响应内容
              
        	
	        //String result = "ok";
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.OPEN_DOOR;
			m.obj = result;
			m.sendToTarget();
			
		} catch (Exception e) {
			
			Log.i("dengying", "Exception=" + e.toString());
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

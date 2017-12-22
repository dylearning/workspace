package com.gi2t.face.detect.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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
import com.gi2t.face.detect.R;
import com.gi2t.face.detect.camera.CameraInterface;
import com.gi2t.face.detect.camera.preview.CameraSurfaceView;
import com.gi2t.face.detect.db.DBHelper;
import com.gi2t.face.detect.mode.GoogleFaceDetect;
import com.gi2t.face.detect.ui.FaceView;
import com.gi2t.face.detect.util.DisplayUtil;
import com.gi2t.face.detect.util.EventUtil;
import com.gi2t.face.detect.util.ImageUtil;
import com.gi2t.face.detect.util.PropertyUtil;
import com.gi2t.face.detect.util.Util;
import com.gi2t.tdmec.ByteUtil;
import com.gi2t.tdmec.MyClient;
import com.gi2t.tdmec.MyClientHandler;
import com.gi2t.tdmec.MyClientThread;
import com.gi2t.tdmec.TdmecMessage;

public class CameraActivity extends Activity{

	private static final String TAG = "CameraActivity";
	private CameraSurfaceView surfaceView = null;
	
	private ImageView img_face;
	private TextView txt_score;
	private TextView txt_usrinfo;
	
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
	
	MyClientThread myClientThread; //netty,socket,tdmec 线程
	
	boolean isCleanData = false;//true false
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		mMainHandler = new MainHandler();
		googleFaceDetect = new GoogleFaceDetect(getApplicationContext(), mMainHandler);//dengying

		mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
		
		mCameraInterface = CameraInterface.getInstance(getApplicationContext(), mMainHandler);
		
		/* 重置数据库*/
		if (isCleanData) {
			new FaceCleanAddDataThread().start();
		}else {
			mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
		}
		
		//开启 netty,socket,tdmec 线程
		//myClientThread = new MyClientThread();
		//myClientThread.start();
		
		MyClient.init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	private void initUI(){
		surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
		faceView = (FaceView)findViewById(R.id.face_view);
		
		img_face = (ImageView)findViewById(R.id.img_face);
		txt_score = (TextView)findViewById(R.id.txt_score);
		txt_usrinfo = (TextView)findViewById(R.id.txt_usrinfo);	
	}
	private void initViewParams(){
		LayoutParams params = surfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		
		//params.width = p.x;//p.x;
		//params.height = p.y/3*2;//p.y; //预览区域调整到3/2高度处
		
		params.width = p.x;
		params.height = p.y;
		
		Log.e("dengying","Screen---Width = " + p.x + "---Height = " +p.y);
		
		previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
		surfaceView.setLayoutParams(params);
		
		faceView.setLayoutParams(params);
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
					
					//坐标转换
					Matrix mMatrix = new Matrix();
					RectF mRect = new RectF();
					boolean isMirror = false;
					int Id = CameraInterface.getInstance().getCameraId();
					if (Id == CameraInfo.CAMERA_FACING_BACK) {
						isMirror = false; // 后置Camera无需mirror
					} else if (Id == CameraInfo.CAMERA_FACING_FRONT) {
						isMirror = true; // 前置Camera需要mirror
					}
					Point p = DisplayUtil.getScreenMetrics(CameraActivity.this);
					Util.prepareMatrix(mMatrix, isMirror, 90, p.x, p.y);

					mMatrix.postRotate(0); // Matrix.postRotate默认是顺时针

					// 只识别一个
					int mRectBottom = 0;
					for (int i = 0; i < 1; i++) {
						mRect.set(faces[i].rect);
						mMatrix.mapRect(mRect);

						int mRectLeft = Math.round(mRect.left);
						int mRectTop = Math.round(mRect.top);
						int mRectRight = Math.round(mRect.right);
						mRectBottom = Math.round(mRect.bottom);

						//Log.e("dengying", "CameraActivity.java,UPDATE_FACE_RECT[" + mRectLeft + "," + mRectTop + "," + mRectRight + "," + mRectBottom + "]");
					}
					
					if(mRectBottom <900){//在固定区域识别才有效
						takePicture();
						Log.e("dengying","takePicture");
						showMessage("检测到人脸，拍照！");
						
						isTakePicture = false;
					}
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
							
				isTakePicture = false;
	
				/* 人脸识别  */
				if(mFaceIdentifyThread == null){
					mFaceIdentifyThread = new FaceIdentifyThread();
					mFaceIdentifyThread.start();
				}
				
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

				showMessage(s_delete_result);
				
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
				}else{	
					s_verify_result = "人脸验证失败，请重新验证！";
				}
				
				showMessage(s_verify_result);
				
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
				
				String s_identify_message="";
				
				int score = 0;
				int i_uid =-1;
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
						
					    score =  (int)Double.parseDouble(scores);
						
					    //从本地数据库，检索出图片
					    i_uid = Integer.parseInt(uid);
					    String pic_url="";
					    Log.e("dengying","i_uid = "+i_uid);
					    if(i_uid>-1){
					    	DBHelper helper = new DBHelper(CameraActivity.this);
										
							android.database.Cursor cursor = helper.queryByUid(i_uid);
							while (cursor.moveToNext()) {
								int id = cursor.getInt(0); // 获取第一列的值,第一列的索引从0开始
								//i_uid = cursor.getInt(1); 
								pic_url = cursor.getString(cursor.getColumnIndex("picurl"));
							}
							cursor.close();
							helper.close();
					    }
					    
					    Log.e("dengying","pic_url = "+pic_url);
					    
					    if(!pic_url.equals("")){
							Bitmap url_bitmap = BitmapFactory.decodeFile(pic_url);
							
							img_face.setImageBitmap(url_bitmap);
					    }
			            
						Log.e("dengying","BAIDU_FACE_IDENTIFY group_id="+group_id+","+",uid="+uid+",user_info="+user_info+",scores="+scores);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("dengying","BAIDU_FACE_IDENTIFY e="+e.toString());
				}
				
				if(error_msg.equals("no user in group")){
					s_identify_message="人脸库没有注册！";
					
					img_face.setImageResource(R.drawable.default_face);
					mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
					
					txt_score.setText("Score:"+score);
					txt_usrinfo.setText("");
				}else if(error_msg.equals("face not found")){
					s_identify_message="人脸没有没有找到，重新开始检测！";
					
					img_face.setImageResource(R.drawable.default_face);
					mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
					
					txt_score.setText("Score:"+score);
					txt_usrinfo.setText("");
				}else if(score < 80){
					s_identify_message = "人脸没有找到合适的人脸库，请重新验证！";
					
					img_face.setImageResource(R.drawable.default_face);
					txt_score.setText("Score:"+score);
					txt_usrinfo.setText("");
					mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
				}else{
					s_identify_message="人脸验证成功，开始开锁！";
					
					//开门
					mOpenDoorThread = new OpenDoorThread();
			        mOpenDoorThread.start();
	
					txt_score.setText("Score:"+score);
					txt_usrinfo.setText(user_info+"\nuid="+uid+"\nscore="+score);
					
					
					//抓拍结果，上传服务器
	        		//ByteBuf byteBuf = Unpooled.buffer(1024);
	        		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
	        		
					json.put("DeviceId", "88881001L");
					json.put("DeviceCode", "88881001L");
					
					if(score < 80){
						json.put("Result", 1);
					}else{
						json.put("Result", 0);
					}
					
					json.put("FaceCordinate", 0);
					json.put("PeopleId", uid);
					json.put("PeopleCode", 0);
					json.put("photoId", 0);
					
					SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm:ss");     
					Date curDate =  new Date(System.currentTimeMillis()); 
					String sDate = formatter.format(curDate);
					json.put("capturetime", sDate);
					
					String base64Img = ImageUtil.bitmaptoString(BitmapFactory.decodeFile(curSaveFileName));//encode bitmaptoString
					Log.e("dengying", "base64Img=" + base64Img);
					
					// 获取需要进行分帧的字段:Photo
					int frameLength = 800;
					if (base64Img != null && !base64Img.equals("")) {
						byte[] infoBeanBytes = base64Img.getBytes();
						int fileDatasNum = 0;
						int fileLengthMax = infoBeanBytes.length;
						int lastFileLength = fileLengthMax % frameLength;
						if (lastFileLength == 0) {
							fileDatasNum = fileLengthMax / frameLength;
						} else {
							fileDatasNum = fileLengthMax / frameLength + 1;
						}
						
						Log.e("dengying", "base64Img,fileDatasNum=" + fileDatasNum);
						
						ByteBuf byteBuf = Unpooled.copiedBuffer(infoBeanBytes);
						for (int i = 1; i <= fileDatasNum; i++) {
							byte[] fileDatas;
							if (i == fileDatasNum) {
								//logger.info ("最后一包数据:" + lastFileLength);
								fileDatas = new byte[lastFileLength];
							} else {
								fileDatas = new byte[frameLength];
							}
							byteBuf.readBytes(fileDatas);
							String content = new String(fileDatas);
							json.put("type", 1002);
							json.put("frameCnt", fileDatasNum);
							json.put("currentCnt", i);
							json.put("Photo", content);
							short msgNo = 0;
							ByteBuf buf = Unpooled.buffer(4 + json.toJSONString().getBytes().length);
							buf.writeShort(ByteUtil.changeByte((short) json.toJSONString().getBytes().length));// 长度
							buf.writeBytes(json.toJSONString().getBytes());
							buf.writeShort(ByteUtil.changeByte(msgNo));
							
							TdmecMessage bm = new TdmecMessage((byte) 0x04, (byte) 0x00, (byte) 0, buf.array());
							MyClientHandler.mChannelHandlerContext.writeAndFlush(bm);
							buf.release();
							
							//Log.e("dengying","writeAndFlush");
						}
						byteBuf.release();
					}
				}
				
				showMessage(s_identify_message);

				break;
				
			case EventUtil.BAIDU_FACE_ADD:
				
				if (mFaceAddThread != null){
					mFaceAddThread.interrupt();
					mFaceAddThread = null;
			     }
				
				int add_uid = (Integer) msg.obj;
				String s_add_result = "";
				
				if(add_uid > -1){
					s_add_result="人脸注册成功！";
					
		            ContentValues values = new ContentValues();
		            values.put("uid", add_uid);
		            values.put("picurl", curSaveFileName);

		            DBHelper helper = new DBHelper(getApplicationContext());
		            boolean insert_ret =helper.insert(values);
		            
		            Log.e("dengying","ret"+insert_ret);
		            
		            //helper.close();
				}else{
					s_add_result="人脸注册失败！";
				}
				
				showMessage(s_add_result);
				
				mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
				
				break;
				
			case EventUtil.MESSAGE_SHOW:
				showMessage((String) msg.obj);
				
				break;
				
			case EventUtil.BAIDU_FACE_MATCH:
				int scoreResult =  (int)Double.parseDouble((String) msg.obj);
				
				Log.e("dengying","UPDATE_BAIDU_SCORE scoreResult="+scoreResult);
					
				showMessage("人脸对比，分数："+scoreResult);
				
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
					showMessage("开门成功，开始下一个验证");
					
					mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
				}else{
					showMessage("开门失败，请检查设备");
					
					mMainHandler.sendEmptyMessageDelayed(EventUtil.FACE_TAKE_PICTURE, 2000);
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
		
		Log.e("dengying","startGoogleFaceDetect");
		
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
	
	private void showMessage(String message){
        Display display = getWindowManager().getDefaultDisplay();
        
        // 获取屏幕高度
        int height = display.getHeight();
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        
        toast.setGravity(Gravity.TOP, 0,0);
        toast.show();
	}
	
	/* 人脸数据库删除*/
	class FaceDeleteThread extends Thread {

		public void run() {
			boolean ret =false;
			
			int i=1;
			ret = FaceDelete.delete(i);
			
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.BAIDU_FACE_DELETE;
			m.obj = ret;
			m.sendToTarget();
		}
	}
	
	/* 人脸注册*/
	class FaceAddThread extends Thread {

		public void run() {
			int add_result = 0;//FaceAdd.add(curSaveFileName);
			
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.BAIDU_FACE_ADD;
			m.obj = add_result;
			m.sendToTarget();
		}
	}
	
	/* 清除数据，添加默认数据 */
	class FaceCleanAddDataThread extends Thread {

		public void run() {
			
			//重置ID
			PropertyUtil.set("gi2t.face.detect.uid", "1");

			//清除本地数据库
			DBHelper helper = new DBHelper(getApplicationContext());
			helper.delAll();
			
			//删除百度数据库
			for(int i=1;i<20;i++){
				boolean ret = FaceDelete.delete(i);
			}
			
			//Context mContext = getApplicationContext();
			
			//int add_result = FaceAdd.add(mContext,"43098765308091","邓迎","男","/storage/emulated/0/PlayCamera/dengying.jpg");
			//add_result = FaceAdd.add(mContext,"43098765308092","张列","男","/storage/emulated/0/PlayCamera/zhanglie.jpg");
			//add_result = FaceAdd.add(mContext,"43098765308093","陈齐刚","男","/storage/emulated/0/PlayCamera/chenqigang.jpg");
			//add_result = FaceAdd.add(mContext,"43098765308094","雷东亮","男","/storage/emulated/0/PlayCamera/leidongliang.jpg");
			
			//添加本地数据库数据
            /*ContentValues values = new ContentValues();
            values.put("uid", 1);
            values.put("picurl", "/storage/emulated/0/PlayCamera/dengying.jpg");
            boolean insert_ret =helper.insert(values);
            Log.e("dengying","ret="+insert_ret);
            
            values = new ContentValues();
            values.put("uid", 2);
            values.put("picurl", "/storage/emulated/0/PlayCamera/zhanglie.jpg");
            insert_ret =helper.insert(values);
            Log.e("dengying","ret="+insert_ret);
            
            values = new ContentValues();
            values.put("uid", 3);
            values.put("picurl", "/storage/emulated/0/PlayCamera/chenqigang.jpg");
            insert_ret =helper.insert(values);
            Log.e("dengying","ret="+insert_ret);
            
            values = new ContentValues();
            values.put("uid", 4);
            values.put("picurl", "/storage/emulated/0/PlayCamera/leidongliang.jpg");
            insert_ret =helper.insert(values);
            Log.e("dengying","ret="+insert_ret);*/
            
            Log.e("dengying","FaceCleanAddDataThread OK");
            
			Message mWait = mMainHandler.obtainMessage();
			mWait.what = EventUtil.MESSAGE_SHOW;
			mWait.obj = "初始化数据成功！";
			mWait.sendToTarget();
		}
	}
	
	/* 人脸验证*/
	class FaceVerifyThread extends Thread {

		public void run() {
			Message mWait = mMainHandler.obtainMessage();
			mWait.what = EventUtil.MESSAGE_SHOW;
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
			mWait.what = EventUtil.MESSAGE_SHOW;
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
				mWait.what = EventUtil.MESSAGE_SHOW;
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
            //String SERVER_URL = "http://192.168.14.149:8111/tdface/door";
        	/*String SERVER_URL = "http://192.168.14.200:8111/tdface/door";
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
	        Log.i("dengying", "openDoor,result = " + result);//获取服务器响应内容*/
            
	        String result = "OK";
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.OPEN_DOOR;
			m.obj = result;
			m.sendToTarget();
		} catch (Exception e) {
			Log.i("dengying", "Exception=" + e.toString());
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			Message m = mMainHandler.obtainMessage();
			m.what = EventUtil.OPEN_DOOR;
			m.obj = e.toString();
			m.sendToTarget();
		}
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
		mMainHandler.removeMessages(EventUtil.MESSAGE_SHOW);
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
		
		if (myClientThread != null) {  
			myClientThread.interrupt();  
			myClientThread = null;  
	     }
		
		stopGoogleFaceDetect();
		mCameraInterface.doStopCamera();
		
		android.os.Process.killProcess(android.os.Process.myPid()); 
		
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}

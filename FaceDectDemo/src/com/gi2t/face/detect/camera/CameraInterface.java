package com.gi2t.face.detect.camera;

import java.io.IOException;
import java.util.List;

import com.gi2t.face.detect.util.CamParaUtil;
import com.gi2t.face.detect.util.EventUtil;
import com.gi2t.face.detect.util.FileUtil;
import com.gi2t.face.detect.util.ImageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraInterface {
	private static final String TAG = "YanZi";
	
	private Context mContext;
	private Handler mHander;
	
	private Camera mCamera;
	private Camera.Parameters mParams;
	private boolean isPreviewing = false;
	private float mPreviwRate = -1f;
	private int mCameraId = -1;
	private boolean isGoolgeFaceDetectOn = false;
	private static CameraInterface mCameraInterface;

	public interface CamOpenOverCallback{
		public void cameraHasOpened();
	}

	
	private CameraInterface(){
	}
	
	private CameraInterface(Context c, Handler handler){
		if(mContext == null){
			mContext = c;
		}
		
		if(mHander == null){
			mHander = handler;
		}
	}
	
	public static synchronized CameraInterface getInstance(){
		if(mCameraInterface == null){
			mCameraInterface = new CameraInterface();
		}
		return mCameraInterface;
	}
	
	public static synchronized CameraInterface getInstance(Context c, Handler handler){
		if(mCameraInterface == null){
			mCameraInterface = new CameraInterface(c,handler);
		}
		return mCameraInterface;
	}
	
	/**打开Camera
	 * @param callback
	 */
	public void doOpenCamera(CamOpenOverCallback callback, int cameraId){
		Log.i(TAG, "Camera open....");
		mCamera = Camera.open(cameraId);
		mCameraId = cameraId;
		if(callback != null){
			callback.cameraHasOpened();
		}
	}
	/**开启预览
	 * @param holder
	 * @param previewRate
	 */
	public void doStartPreview(SurfaceHolder holder, float previewRate){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}
		if(mCamera != null){

			mParams = mCamera.getParameters();
			mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
			CamParaUtil.getInstance().printSupportPictureSize(mParams);
			CamParaUtil.getInstance().printSupportPreviewSize(mParams);
			//设置PreviewSize和PictureSize
			Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(mParams.getSupportedPictureSizes(),previewRate, 800);
			//mParams.setPictureSize(pictureSize.width, pictureSize.height);
			mParams.setPictureSize(1280, 720);
			Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(mParams.getSupportedPreviewSizes(), previewRate, 800);
			//mParams.setPreviewSize(previewSize.width, previewSize.height);
			mParams.setPreviewSize(864, 480);
			
			Log.i("dengying", "CameraInterface.java doStartPreview pictureSize:width="+pictureSize.width+",height="+pictureSize.height);
			Log.i("dengying", "CameraInterface.java doStartPreview previewSize:width="+previewSize.width+",height="+previewSize.height);
			
			mCamera.setDisplayOrientation(90);

			CamParaUtil.getInstance().printSupportFocusMode(mParams);
			List<String> focusModes = mParams.getSupportedFocusModes();
			if(focusModes.contains("continuous-video")){
				mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}
			mCamera.setParameters(mParams);	

			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();//开启预览
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters(); //重新get一次
			Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}
	/**
	 * 停止预览，释放Camera
	 */
	public void doStopCamera(){
		if(null != mCamera)
		{
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview(); 
			isPreviewing = false; 
			mPreviwRate = -1f;
			mCamera.release();
			mCamera = null;     
		}
	}
	/**
	 * 拍照
	 */
	public void doTakePicture(){
		if(isPreviewing && (mCamera != null)){
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
		}
	}
	
	/**获取Camera.Parameters
	 * @return
	 */
	public Camera.Parameters getCameraParams(){
		if(mCamera != null){
			mParams = mCamera.getParameters();
			return mParams;
		}
		return null;
	}
	/**获取Camera实例
	 * @return
	 */
	public Camera getCameraDevice(){
		return mCamera;
	}
	

	public int getCameraId(){
		return mCameraId;
	}
	

	/*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
	ShutterCallback mShutterCallback = new ShutterCallback() 
	//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
	{
		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	PictureCallback mRawCallback = new PictureCallback() 
	// 拍摄的未压缩原数据的回调,可以为null
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myRawCallback:onPictureTaken...");

		}
	};
	PictureCallback mJpegPictureCallback = new PictureCallback() 
	//对jpeg图像数据的回调,最重要的一个回调
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//保存图片到sdcard
			if(null != b)
			{
				//设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
				//图片竟然不能旋转了，故这里要旋转下
				Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 270.0f);
				String mFileName = FileUtil.saveBitmap(rotaBitmap, mContext);
				
				Log.i("dengying", "save picture!!!");
				
				if(!mFileName.equals("")){
					Message m = mHander.obtainMessage();
					m.what = EventUtil.TAKE_PICTURE_FILENAME;
					m.obj = mFileName;
					m.sendToTarget();
				}
			}
			//再次进入预览
			mCamera.startPreview();
			isPreviewing = true;
		}
	};


}

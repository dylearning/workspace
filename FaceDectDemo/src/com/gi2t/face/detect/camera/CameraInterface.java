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
	
	/**��Camera
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
	/**����Ԥ��
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
			mParams.setPictureFormat(PixelFormat.JPEG);//�������պ�洢��ͼƬ��ʽ
			CamParaUtil.getInstance().printSupportPictureSize(mParams);
			CamParaUtil.getInstance().printSupportPreviewSize(mParams);
			//����PreviewSize��PictureSize
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
				mCamera.startPreview();//����Ԥ��
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters(); //����getһ��
			Log.i(TAG, "��������:PreviewSize--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "��������:PictureSize--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}
	/**
	 * ֹͣԤ�����ͷ�Camera
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
	 * ����
	 */
	public void doTakePicture(){
		if(isPreviewing && (mCamera != null)){
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
		}
	}
	
	/**��ȡCamera.Parameters
	 * @return
	 */
	public Camera.Parameters getCameraParams(){
		if(mCamera != null){
			mParams = mCamera.getParameters();
			return mParams;
		}
		return null;
	}
	/**��ȡCameraʵ��
	 * @return
	 */
	public Camera getCameraDevice(){
		return mCamera;
	}
	

	public int getCameraId(){
		return mCameraId;
	}
	

	/*Ϊ��ʵ�����յĿ������������ձ�����Ƭ��Ҫ���������ص�����*/
	ShutterCallback mShutterCallback = new ShutterCallback() 
	//���Ű��µĻص������������ǿ����������Ʋ��š����ꡱ��֮��Ĳ�����Ĭ�ϵľ������ꡣ
	{
		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	PictureCallback mRawCallback = new PictureCallback() 
	// �����δѹ��ԭ���ݵĻص�,����Ϊnull
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myRawCallback:onPictureTaken...");

		}
	};
	PictureCallback mJpegPictureCallback = new PictureCallback() 
	//��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data���ֽ����ݣ����������λͼ
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//����ͼƬ��sdcard
			if(null != b)
			{
				//����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation", 90)ʧЧ��
				//ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
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
			//�ٴν���Ԥ��
			mCamera.startPreview();
			isPreviewing = true;
		}
	};


}

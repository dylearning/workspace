package com.gi2t.face.detect.ui;

import com.gi2t.face.detect.camera.CameraInterface;
import com.gi2t.face.detect.R;
import com.gi2t.face.detect.util.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class FaceView extends ImageView {
	private static final String TAG = "FaceView";
	private Context mContext;
	private Paint mLinePaint;
	private Face[] mFaces;
	private Matrix mMatrix = new Matrix();
	private RectF mRect = new RectF();
	private Drawable mFaceIndicator = null;
	
	public static int mRectLeft=0;
	public static int mRectTop=0;
	public static int mRectRight=0;
	public static int mRectBottom=0;
	
	public FaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initPaint();
		mContext = context;
		mFaceIndicator = getResources().getDrawable(R.drawable.ic_face_find_2);
	}


	public void setFaces(Face[] faces){
		this.mFaces = faces;
		invalidate();
	}
	public void clearFaces(){
		mFaces = null;
		invalidate();
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if(mFaces == null || mFaces.length < 1){
			return;
		}
		
		//Log.e("dengying","FaceView,onDraw ["+Math.round(mFaces[0].rect.left)+","+Math.round(mFaces[0].rect.top)+","+Math.round(mFaces[0].rect.right)+","+Math.round(mFaces[0].rect.bottom)+"]");
		
		boolean isMirror = false;
		int Id = CameraInterface.getInstance().getCameraId();
		if(Id == CameraInfo.CAMERA_FACING_BACK){
			isMirror = false; //����Camera����mirror
		}else if(Id == CameraInfo.CAMERA_FACING_FRONT){
			isMirror = true;  //ǰ��Camera��Ҫmirror
		}
		Util.prepareMatrix(mMatrix, isMirror, 90, getWidth(), getHeight());
		canvas.save();
		mMatrix.postRotate(0); //Matrix.postRotateĬ����˳ʱ��
		canvas.rotate(-0);   //Canvas.rotate()Ĭ������ʱ�� 
		
		//ֻʶ��һ��
		for(int i = 0; i< 1;/*mFaces.length*/ i++){
			mRect.set(mFaces[i].rect);
			mMatrix.mapRect(mRect);
			
            mRectLeft = Math.round(mRect.left);
            mRectTop = Math.round(mRect.top);
            mRectRight = Math.round(mRect.right);
            mRectBottom = Math.round(mRect.bottom);
			
            mFaceIndicator.setBounds(mRectLeft, mRectTop,mRectRight,mRectBottom);
            
            if(mRectBottom <900)//�ڹ̶�����ʶ�����Ч
            	mFaceIndicator.draw(canvas);
            
            //Log.e("dengying","FaceView,onDraw["+Math.round(mRect.left)+","+Math.round(mRect.top)+","+Math.round(mRect.right)+","+Math.round(mRect.bottom)+"]");
//			canvas.drawRect(mRect, mLinePaint);
		}
		canvas.restore();
		super.onDraw(canvas);
	}

	private void initPaint(){
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		int color = Color.rgb(0, 150, 255);
		int color = Color.rgb(98, 212, 68);
//		mLinePaint.setColor(Color.RED);
		mLinePaint.setColor(color);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(5f);
		mLinePaint.setAlpha(180);
	}
}

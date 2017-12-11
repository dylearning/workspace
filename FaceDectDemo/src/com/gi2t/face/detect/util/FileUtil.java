package com.gi2t.face.detect.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gi2t.face.detect.activity.CameraActivity;

import com.baidu.ai.aip.demo.FaceMatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class FileUtil {
	private static final  String TAG = "FileUtil";
	private static final File parentPath = Environment.getExternalStorageDirectory();
	private static   String storagePath = "";
	private static final String DST_FOLDER_NAME = "PlayCamera";
	
	private static String preSaveFileName="";

	/**��ʼ������·��
	 * @return
	 */
	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;
			File f = new File(storagePath);
			if(!f.exists()){
				boolean ret =f.mkdirs();
				
				Log.i("dengying", "initPath mkdirs ret=" + ret);
			}
		}
		return storagePath;
	}

	/**����Bitmap��sdcard
	 * @param b
	 */
	public static String saveBitmap(Bitmap b,Context mContext){

		String mFileName = "";
		
		String path = initPath();
		long dataTake = System.currentTimeMillis();
		String jpegName = path + "/" + dataTake +".jpg";
		Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			Log.i(TAG, "saveBitmap�ɹ�");
			
			mFileName = jpegName;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "saveBitmap:ʧ��");
			e.printStackTrace();
		}

		return mFileName;
	}

	
	/**�����ļ��Ա�·��
	 * @return
	 */
	public static String getCompFile1(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;
		}
		
		return storagePath+"01.jpg";
	}

	/**�����ļ��Ա�·��
	 * @return
	 */
	public static String getCompFile2(){
		if(storagePath.equals("")){
			storagePath = parentPath.getAbsolutePath()+"/" + DST_FOLDER_NAME;
		}
		
		return storagePath+"02.jpg";
	}
}

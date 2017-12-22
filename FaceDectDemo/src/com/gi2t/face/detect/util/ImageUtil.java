package com.gi2t.face.detect.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

//import org.apache.commons.codec.binary.Base64;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

public class ImageUtil {
	
	//��תBitmap
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
		Matrix matrix = new Matrix();
		matrix.postRotate((float)rotateDegree);
		Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
		return rotaBitmap;
	}
	
	// ��base64�����ַ���ת����Bitmap����
	public static Bitmap stringtoBitmap(String string) {
		
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(string, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}
	
	//��Bitmapת����base64�����ַ���
	public static String bitmaptoString(Bitmap bitmap) {
		String string = null;
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, bStream);
		byte[] bytes = bStream.toByteArray();
		string = Base64.encodeToString(bytes, Base64.DEFAULT);
		return string;
	}
	
	
	/**
	 * �����������ݱ���ΪBASE64�ַ���
	 * 
	 * @param binaryData
	 * @return
	 */
	public static String encode(Bitmap bitmap) {
		try {
			String string = null;
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, 100, bStream);
			byte[] binaryData = bStream.toByteArray();
			
			return new String(org.apache.commons.codec.binary.Base64.encodeBase64(binaryData), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * ��BASE64�ַ����ָ�Ϊ����������
	 * 
	 * @param base64String
	 * @return
	 */
	public static byte[] decode(String base64String) {
		try {
			return org.apache.commons.codec.binary.Base64.decodeBase64(base64String.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

}

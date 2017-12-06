package com.baidu.ai.aip.demo;

import java.net.URLEncoder;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

/**
 * ����̽��
 */
public class FaceDetect {

	private static String baiduToken;
	
	/**
	 * ��Ҫ��ʾ���������蹤���� FileUtil,Base64Util,HttpUtil,GsonUtils���
	 * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
	 * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
	 * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
	 * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3 ����
	 */
	public static String detect() {
		
		new Thread() {
			@Override
			public void run() {
				// ��������ʵĴ����������
				baiduToken = AuthService.getAuth();
			}
		}.start();
		
		// ����url
		String url = "https://aip.baidubce.com/rest/2.0/face/v1/detect";
		try {
			// �����ļ�·��
			String filePath = "[�����ļ�·��]";
			byte[] imgData = FileUtil.readFileByBytes(filePath);
			String imgStr = Base64Util.encode(imgData);
			String imgParam = URLEncoder.encode(imgStr, "UTF-8");

			String param = "max_face_num=" + 5 + "&face_fields=" + "age,beauty,expression,faceshape,gender,glasses,landmark,race,qualities" + "&image=" + imgParam;

			// ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬
			// �ͻ��˿����л��棬���ں����»�ȡ��
			String accessToken = "[���ü�Ȩ�ӿڻ�ȡ��token]";

			String result = HttpUtil.post(url, accessToken, param);
			System.out.println(result);

			Log.e("dengying", "detect result=" + result);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

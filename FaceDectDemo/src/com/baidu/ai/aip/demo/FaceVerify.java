package com.baidu.ai.aip.demo;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ������֤
 */
public class FaceVerify {

	private static String baiduToken;
	private static String baiduResult;
	private static String scoreResult;
	
	/**
	 * ��Ҫ��ʾ���������蹤���� FileUtil,Base64Util,HttpUtil,GsonUtils���
	 * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
	 * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
	 * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
	 * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3 ����
	 */
	public static String verify(String filePath) {
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceVerify,baiduToken=" + baiduToken);
		
		// ����url
		String url = "https://aip.baidubce.com/rest/2.0/face/v2/verify";
		try {
			// �����ļ�·��
			//String filePath = "[�����ļ�·��]";
			byte[] imgData = FileUtil.readFileByBytes(filePath);
			String imgStr = Base64Util.encode(imgData);
			String imgParam = URLEncoder.encode(imgStr, "UTF-8");

			//String filePath2 = "[�����ļ�·��]";
			//byte[] imgData2 = FileUtil.readFileByBytes(filePath2);
			//String imgStr2 = Base64Util.encode(imgData2);
			//String imgParam2 = URLEncoder.encode(imgStr2, "UTF-8");

			String param = "uid=" + "test_user_5" + "&top_num=" + 1 + "&images=" + imgParam /*+ "," + imgParam2*/;

			// ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬
			// �ͻ��˿����л��棬���ں����»�ȡ��
			String accessToken = baiduToken;//"[���ü�Ȩ�ӿڻ�ȡ��token]";

			String result = HttpUtil.post(url, accessToken, param);
			System.out.println(result);
			
			//����ʾ��
			//{
			//    "log_id": 73473737,
			//    "result_num":2,
			//    "result": [
			//           99.3,
			//           83.6
			//    ]
			//}
			//�Ƽ��÷ֳ���80����Ϊ��֤�ɹ�
			
			
			//ʧ��{"error_code":216611,"error_msg":"user not exist","log_id":3198748889120510}
			
			Log.e("dengying","FaceVerify verify="+result);
			
			result = getVerifyScore(result);
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getVerifyScore(String s){
		String result = "";
		//String error_msg ="";
		try {
			JSONObject root = new JSONObject(s);
	        
			result = root.getString("result");
			
			int length =result.length();
			result = result.substring(1,(length-1));
			
			//error_msg = root.getString("error_msg");
			//if(error_msg.equals("")){
			//	result = "0";
			//}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result == null || result.equals("")) {
			result = "0";
		}
		
		Log.e("dengying","getVerifyScore result="+result);
		
        return result;
	}

}
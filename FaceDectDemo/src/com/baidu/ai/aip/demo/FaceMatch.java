package com.baidu.ai.aip.demo;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

/**
 * ����̽��
 */
public class FaceMatch {

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
	public static String match(final String filePath, final String filePath2) {

		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceMatch,baiduToken=" + baiduToken);

		// ����url
		String url = "https://aip.baidubce.com/rest/2.0/face/v2/match";
		try {
	
			// �����ļ�·��
			// String filePath = "[�����ļ�·��]";
			byte[] imgData = FileUtil.readFileByBytes(filePath);
			String imgStr = Base64Util.encode(imgData);
			String imgParam = URLEncoder.encode(imgStr, "UTF-8");

			// String filePath2 = "[�����ļ�·��]";
			byte[] imgData2 = FileUtil.readFileByBytes(filePath2);
			String imgStr2 = Base64Util.encode(imgData2);
			String imgParam2 = URLEncoder.encode(imgStr2, "UTF-8");


			String param = "images=" + imgParam + "," + imgParam2;

			// ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬
			// �ͻ��˿����л��棬���ں����»�ȡ��
			String accessToken = baiduToken;// "[���ü�Ȩ�ӿڻ�ȡ��token]";

			Log.e("dengying", "FaceMatch,HttpUtil.post");
			
			String result = HttpUtil.post(url, accessToken, param);
			//System.out.println(result);

			Log.e("dengying", "FaceMatch,match,result=" + result);
			
			baiduResult = result;
			scoreResult = doJsonScore(result);

		} catch (Exception e) {
			e.printStackTrace();
			Log.e("dengying", "FaceMatch,match,Exception=" + e.toString());
		}

		return scoreResult;
	}

	private static String doJsonScore(String s){
		
		String score="";
		
		try {
			JSONObject root = new JSONObject(s);
			
			//Log.e("dengying","doJsonScore json="+root.getString("result")+",result_num="+root.getString("result_num")+",log_id="+root.getString("log_id"));
			
	        
	        //��ȡ�������
	        JSONArray array = root.getJSONArray("result");
	        for (int i = 0; i < array.length(); i++) {
	            JSONObject result = array.getJSONObject(i);
	            
	            score = result.getString("score");
				//Log.e("dengying","doJsonScore result:index_i="+result.getInt("index_i")+",index_j="+result.getString("index_j")+",score="+result.getString("score"));     
	        }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return score;
	}
}

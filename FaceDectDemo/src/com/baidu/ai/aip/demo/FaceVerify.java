package com.baidu.ai.aip.demo;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 人脸认证
 */
public class FaceVerify {

	private static String baiduToken;
	private static String baiduResult;
	private static String scoreResult;
	
	/**
	 * 重要提示代码中所需工具类 FileUtil,Base64Util,HttpUtil,GsonUtils请从
	 * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
	 * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
	 * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
	 * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3 下载
	 */
	public static String verify(String filePath) {
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceVerify,baiduToken=" + baiduToken);
		
		// 请求url
		String url = "https://aip.baidubce.com/rest/2.0/face/v2/verify";
		try {
			// 本地文件路径
			//String filePath = "[本地文件路径]";
			byte[] imgData = FileUtil.readFileByBytes(filePath);
			String imgStr = Base64Util.encode(imgData);
			String imgParam = URLEncoder.encode(imgStr, "UTF-8");

			//String filePath2 = "[本地文件路径]";
			//byte[] imgData2 = FileUtil.readFileByBytes(filePath2);
			//String imgStr2 = Base64Util.encode(imgData2);
			//String imgParam2 = URLEncoder.encode(imgStr2, "UTF-8");

			String param = "uid=" + "test_user_5" + "&top_num=" + 1 + "&images=" + imgParam /*+ "," + imgParam2*/;

			// 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间，
			// 客户端可自行缓存，过期后重新获取。
			String accessToken = baiduToken;//"[调用鉴权接口获取的token]";

			String result = HttpUtil.post(url, accessToken, param);
			System.out.println(result);
			
			//返回示例
			//{
			//    "log_id": 73473737,
			//    "result_num":2,
			//    "result": [
			//           99.3,
			//           83.6
			//    ]
			//}
			//推荐得分超过80可认为认证成功
			
			
			//失败{"error_code":216611,"error_msg":"user not exist","log_id":3198748889120510}
			
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
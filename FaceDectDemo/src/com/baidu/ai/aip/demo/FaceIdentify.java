package com.baidu.ai.aip.demo;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;


/**
* 人脸查找——识别
*/
public class FaceIdentify {

	private static String baiduToken;
	private static String baiduResult;
	private static String scoreResult;
	
    /**
    * 重要提示代码中所需工具类
    * FileUtil,Base64Util,HttpUtil,GsonUtils请从
    * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
    * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
    * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
    * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
    * 下载
    */
    public static String identify(String filePath) {
    	String result = "";
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceIdentify,baiduToken=" + baiduToken);
    	
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/identify";
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

            String param = "group_id=" + "test_group_2" + "&user_top_num=" + "1" + "&face_top_num=" + "1" + "&images=" + imgParam /*+ "," + imgParam2*/;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduToken;//"[调用鉴权接口获取的token]";

            result = HttpUtil.post(url, accessToken, param);
            
            //返回示例
            //{
            //    "log_id": 73473737,
            //    "result_num":1,
            //    "result": [
            //        {
            //            "group_id" : "test1",
            //            "uid": "u333333",
            //            "user_info": "Test User",
            //            "scores": [
            //                    99.3,
            //                    83.4
            //            ]
            //        }
            //    ]
            //}
            
            //{"error_code":216618,"error_msg":"no user in group","log_id":3975955222120618}
            
            Log.e("dengying","FaceIdentify result="+result);
            
            //result = getIdentifyResult(result);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	private static String getIdentifyResult(String s){
		String result = "";
		
		try {
			JSONObject root = new JSONObject(s);
	        
			result = root.getString("error_msg");
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			//ret = false;
		}

		Log.e("dengying","getAddResult ret="+result);
		
        return result;
	}

}

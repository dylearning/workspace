package com.baidu.ai.aip.demo;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;


/**
* �������ҡ���ʶ��
*/
public class FaceIdentify {

	private static String baiduToken;
	private static String baiduResult;
	private static String scoreResult;
	
    /**
    * ��Ҫ��ʾ���������蹤����
    * FileUtil,Base64Util,HttpUtil,GsonUtils���
    * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
    * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
    * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
    * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
    * ����
    */
    public static String identify(String filePath) {
    	String result = "";
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceIdentify,baiduToken=" + baiduToken);
    	
        // ����url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/identify";
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

            String param = "group_id=" + "test_group_2" + "&user_top_num=" + "1" + "&face_top_num=" + "1" + "&images=" + imgParam /*+ "," + imgParam2*/;

            // ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬 �ͻ��˿����л��棬���ں����»�ȡ��
            String accessToken = baiduToken;//"[���ü�Ȩ�ӿڻ�ȡ��token]";

            result = HttpUtil.post(url, accessToken, param);
            
            //����ʾ��
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

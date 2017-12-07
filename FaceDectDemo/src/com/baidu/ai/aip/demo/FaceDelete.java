package com.baidu.ai.aip.demo;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
* ɾ���û�
*/
public class FaceDelete {
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
    public static boolean delete() {
    	boolean ret = false;
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceDelete,baiduToken=" + baiduToken);
    	
        // ����url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/delete";
        try {
            String param = "uid=" + "1";

            // ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬 �ͻ��˿����л��棬���ں����»�ȡ��
            String accessToken = baiduToken;//"[���ü�Ȩ�ӿڻ�ȡ��token]";

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            
            
            //����ʾ��
            // ɾ���ɹ�
            //{
            //    "log_id": 73473737,
            //}
            // ɾ����������
            //{
            //  "error_code": 216612,
            //  "log_id": 1382953199,
            //  "error_msg": "user not exist"
            //}
            
            Log.e("dengying","FaceDelete result="+result);
            
            
            ret = getDeleteResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            
            ret = false;
        }
        return ret;
    }
    
	private static boolean getDeleteResult(String s){
		boolean ret = true;
		
		try {
			JSONObject root = new JSONObject(s);
	        
			String error_code = root.getString("error_code");
			
			if(error_code == null || error_code.equals("")){
				ret = true;
			}else{
				ret = false;
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			ret = true;
		}

		Log.e("dengying","getDeleteResult ret="+ret);
		
        return ret;
	}
}
package com.baidu.ai.aip.demo;

import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

/**
* ����ע��
*/
public class FaceAdd {

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
    public static boolean add(String filePath) {
    	boolean ret = false;
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceAdd,baiduToken=" + baiduToken);
    	
        // ����url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add";
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

            String param = "uid=" + "test_user_5" + "&user_info=" + "userInfo5" + "&group_id=" + "test_group_2" + "&images=" + imgParam /*+ "," + imgParam2*/;

            // ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬 �ͻ��˿����л��棬���ں����»�ȡ��
            String accessToken = baiduToken;//"[���ü�Ȩ�ӿڻ�ȡ��token]";

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            
            //����ʾ��
	        // ע��ɹ�
	        //{
	        //    "log_id": 73473737,
	        //}
	        // ע�ᷢ������
	        //{
	        //  "error_code": 216616,
	        //  "log_id": 674786177,
	        //  "error_msg": "image exist"
	        //}
            
            Log.e("dengying","FaceAdd result="+result);
            
            return getAddResult(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("dengying","FaceAdd Exception="+e.toString());
        }
        return ret;
    }
    
    
	private static boolean getAddResult(String s){
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
			
			//ret = false;
		}

		Log.e("dengying","getAddResult ret="+ret);
		
        return ret;
	}

}

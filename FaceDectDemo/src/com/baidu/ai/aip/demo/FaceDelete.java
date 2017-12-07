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
* 删除用户
*/
public class FaceDelete {
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
    public static boolean delete() {
    	boolean ret = false;
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceDelete,baiduToken=" + baiduToken);
    	
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/delete";
        try {
            String param = "uid=" + "1";

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduToken;//"[调用鉴权接口获取的token]";

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            
            
            //返回示例
            // 删除成功
            //{
            //    "log_id": 73473737,
            //}
            // 删除发生错误
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
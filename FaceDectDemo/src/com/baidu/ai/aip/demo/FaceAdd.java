package com.baidu.ai.aip.demo;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;
import com.gi2t.face.detect.activity.CameraActivity;
import com.gi2t.face.detect.util.PropertyUtil;

/**
* 人脸注册
*/
public class FaceAdd {

	private static String baiduToken;
	private static String baiduResult;
	private static String scoreResult;
	
	private static int uid = 1;
	
    /**
    * 重要提示代码中所需工具类
    * FileUtil,Base64Util,HttpUtil,GsonUtils请从
    * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
    * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
    * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
    * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
    * 下载
    */
    public static int add(String filePath) {
    	int ret = -1;
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceAdd,baiduToken=" + baiduToken);
    	
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add";
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
            
            uid = Integer.parseInt(PropertyUtil.get("gi2t.face.detect.uid", "1")) ;
            
            String param = "uid=" + uid + "&user_info=" + "新注册用户"+ uid + "&group_id=" + "test_group_2" + "&images=" + imgParam /*+ "," + imgParam2*/;

            if(filePath.equals("/storage/emulated/0/PlayCamera/dengying.jpg")){
            	param = "uid=" + uid + "&user_info=" + "邓迎"+ 1 + "&group_id=" + "test_group_2" + "&images=" + imgParam;
            }else if(filePath.equals("/storage/emulated/0/PlayCamera/zhanglie.jpg")){
            	param = "uid=" + uid + "&user_info=" + "张列"+ 2 + "&group_id=" + "test_group_2" + "&images=" + imgParam;
            }if(filePath.equals("/storage/emulated/0/PlayCamera/chenqigang.jpg")){
            	param = "uid=" + uid + "&user_info=" + "陈齐刚"+ 3 + "&group_id=" + "test_group_2" + "&images=" + imgParam;
            }if(filePath.equals("/storage/emulated/0/PlayCamera/leidongliang.jpg")){
            	param = "uid=" + uid + "&user_info=" + "雷东亮"+ 4 + "&group_id=" + "test_group_2" + "&images=" + imgParam;
            }
            
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduToken;//"[调用鉴权接口获取的token]";

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            
            //返回示例
	        // 注册成功
	        //{
	        //    "log_id": 73473737,
	        //}
	        // 注册发生错误
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
    
    
	private static int getAddResult(String s){
		int ret = -1;

		try {
			JSONObject root = new JSONObject(s);
	        
			String error_code = root.getString("error_code");
			
			if(error_code == null || error_code.equals("")){
				//ret = uid;
			}else{
				ret = -1;
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			ret = uid;
			PropertyUtil.set("gi2t.face.detect.uid", String.valueOf(++uid));
		}

		Log.e("dengying","getAddResult uid="+ret);
		
        return ret;
	}

}

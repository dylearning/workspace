package com.baidu.ai.aip.demo;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;
import com.gi2t.face.detect.activity.CameraActivity;
import com.gi2t.face.detect.activity.MyApplication;
import com.gi2t.face.detect.db.DBHelper;
import com.gi2t.face.detect.util.PropertyUtil;

/**
* 人脸更新
*/
public class FaceUpdate {

	private static String baiduToken;
	
    public static int update(int uid,String filePath) {
    	int ret_uid =-1;
    	boolean ret = false;
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceUpdate,baiduToken=" + baiduToken);
    	
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/update";
        try {
            // 本地文件路径
            //String filePath = "[本地文件路径]";
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            /*String filePath2 = "[本地文件路径]";
            byte[] imgData2 = FileUtil.readFileByBytes(filePath2);
            String imgStr2 = Base64Util.encode(imgData2);
            String imgParam2 = URLEncoder.encode(imgStr2, "UTF-8");*/

            String param = "uid=" + uid + "&images=" + imgParam /*+ "," + imgParam2*/;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduToken;//"[调用鉴权接口获取的token]";

            String result = HttpUtil.post(url, accessToken, param);
            
            Log.e("dengying", "FaceUpdate ret=" + result);
            
            ret = getUpdateResult(result);
            
			if (ret) {
				// 更新本地数据库数据
				DBHelper helper = new DBHelper(MyApplication.getContext());
				ContentValues values = new ContentValues();
				values.put("uid", uid);
				values.put("picurl", filePath);
				boolean update_ret = helper.updateByUid(uid,values);
				
				ret_uid = uid;
				Log.e("dengying", "FaceUpdate sql ret=" + update_ret);
			}
            
            // 更新成功
            //{
            //    "log_id": 73473737,
            //}
            // 更新发生错误
            //{
            //  "error_code": 216612,
            //  "log_id": 1137508902,
            //  "error_msg": "user not exist"
            //}
  
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret_uid;
    }
	
	private static boolean getUpdateResult(String s) {
		boolean ret = false;

		try {
			JSONObject root = new JSONObject(s);

			String error_code = root.getString("error_code");

			ret = false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			ret = true;
		}

		Log.e("dengying", "getUpdateResult ret=" + ret);

		return ret;
	}

}

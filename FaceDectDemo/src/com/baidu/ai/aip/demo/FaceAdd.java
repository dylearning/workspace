package com.baidu.ai.aip.demo;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;

import com.baidu.ai.aip.utils.Base64Util;
import com.baidu.ai.aip.utils.FileUtil;
import com.baidu.ai.aip.utils.HttpUtil;
import com.gi2t.face.detect.activity.CameraActivity;
import com.gi2t.face.detect.db.DBHelper;
import com.gi2t.face.detect.util.PropertyUtil;

/**
* 人脸注册
*/
public class FaceAdd {

	private static String baiduToken;
	private static String baiduResult;
	private static String scoreResult;
	
	private static int uid = -1;
	
    /**
    * 重要提示代码中所需工具类
    * FileUtil,Base64Util,HttpUtil,GsonUtils请从
    * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
    * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
    * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
    * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
    * 下载
    */

    public static int add(Context mContext,String id_code,String name,String sex,String filePath) {
    	int ret = -1;
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceAdd,baiduToken=" + baiduToken);
    	
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add";
        try {
        	DBHelper helper = new DBHelper(mContext);
        	
        	boolean isNew = true;//是否新增
        	
        	Cursor cursor = helper.queryByPeopleCode(id_code);
      
			while (cursor.moveToNext()) {
				uid = cursor.getInt(cursor.getColumnIndex("uid")); 
				isNew = false;
			}
			cursor.close();
        		
        	
        	if(isNew){
	            // 本地文件路径
	            byte[] imgData = FileUtil.readFileByBytes(filePath);
	            String imgStr = Base64Util.encode(imgData);
	            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
	
	            uid = Integer.parseInt(PropertyUtil.get("gi2t.face.detect.uid", "1"));
	            
				String param = "uid=" + uid + "&user_info=" + "uid:" + uid + "\nid_code:" + id_code + "\nname：" + name + "\nsex:" + sex + "&group_id=" + "test_group_2" + "&images=" + imgParam;
	
	            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
	            String accessToken = baiduToken;//"[调用鉴权接口获取的token]";
	
	            String result = HttpUtil.post(url, accessToken, param);
	            
	            Log.e("dengying","FaceAdd result="+result);
	            
	            ret = getAddResult(result);
	            
				if (ret > -1) {
					// 添加本地数据库数据
					ContentValues values = new ContentValues();
					values.put("uid", ret);
					values.put("peoplecode", id_code);
					values.put("picurl", filePath);
					boolean insert_ret = helper.insert(values);
					Log.e("dengying", "ret=" + insert_ret);
				}
        	}else{
        		ret = FaceUpdate.update(uid, filePath);
        	}
            
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

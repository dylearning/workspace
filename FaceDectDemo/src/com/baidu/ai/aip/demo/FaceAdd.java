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
* ����ע��
*/
public class FaceAdd {

	private static String baiduToken;
	private static String baiduResult;
	private static String scoreResult;
	
	private static int uid = -1;
	
    /**
    * ��Ҫ��ʾ���������蹤����
    * FileUtil,Base64Util,HttpUtil,GsonUtils���
    * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
    * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
    * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
    * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
    * ����
    */

    public static int add(Context mContext,String id_code,String name,String sex,String filePath) {
    	int ret = -1;
    	
		baiduToken = AuthService.getAuth();
		Log.e("dengying", "FaceAdd,baiduToken=" + baiduToken);
    	
        // ����url
        String url = "https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add";
        try {
        	DBHelper helper = new DBHelper(mContext);
        	
        	boolean isNew = true;//�Ƿ�����
        	
        	Cursor cursor = helper.queryByPeopleCode(id_code);
      
			while (cursor.moveToNext()) {
				uid = cursor.getInt(cursor.getColumnIndex("uid")); 
				isNew = false;
			}
			cursor.close();
        		
        	
        	if(isNew){
	            // �����ļ�·��
	            byte[] imgData = FileUtil.readFileByBytes(filePath);
	            String imgStr = Base64Util.encode(imgData);
	            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
	
	            uid = Integer.parseInt(PropertyUtil.get("gi2t.face.detect.uid", "1"));
	            
				String param = "uid=" + uid + "&user_info=" + "uid:" + uid + "\nid_code:" + id_code + "\nname��" + name + "\nsex:" + sex + "&group_id=" + "test_group_2" + "&images=" + imgParam;
	
	            // ע�������Ϊ�˼򻯱���ÿһ������ȥ��ȡaccess_token�����ϻ���access_token�й���ʱ�䣬 �ͻ��˿����л��棬���ں����»�ȡ��
	            String accessToken = baiduToken;//"[���ü�Ȩ�ӿڻ�ȡ��token]";
	
	            String result = HttpUtil.post(url, accessToken, param);
	            
	            Log.e("dengying","FaceAdd result="+result);
	            
	            ret = getAddResult(result);
	            
				if (ret > -1) {
					// ��ӱ������ݿ�����
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

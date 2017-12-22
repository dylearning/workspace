package com.gi2t.face.detect.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "facedetect.db";
	private static final int version = 1; 
	private static final String TBL_NAME = "face";
	private static final String CREATE_TBL = " create table " + TBL_NAME + "(_id integer primary key autoincrement, uid integer,peoplecode text,picurl text) ";
	private SQLiteDatabase db;
	
	private static final String TAG = "dengying";
	
	/*
	qlite3 database.db
	sqlite> create table admin(username text,age integer);
	sqlite> insert into admin values('kuang',25);
	sqlite> select * from admin;
	sqlite> update admin set username='kk',age=24 where username='kuang' and age=25;
	sqlite> delete from admin where username='kk';
	*/
	  

	public DBHelper(Context c) {
		super(c, DB_NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		try {
			db.execSQL(CREATE_TBL);
		} catch (SQLException ex) {
			Log.d(TAG, "create table failure");
		}
		
       /* if(filePath.equals("/storage/emulated/0/PlayCamera/dengying.jpg")){
        	param = "uid=" + uid + "&user_info=" + "µÀ”≠"+ 1 + "&group_id=" + "test_group_2" + "&images=" + imgParam;
        }else if(filePath.equals("/storage/emulated/0/PlayCamera/zhanglie.jpg")){
        	param = "uid=" + uid + "&user_info=" + "’≈¡–"+ 2 + "&group_id=" + "test_group_2" + "&images=" + imgParam;
        }if(filePath.equals("/storage/emulated/0/PlayCamera/chenqigang.jpg")){
        	param = "uid=" + uid + "&user_info=" + "≥¬∆Î∏’"+ 3 + "&group_id=" + "test_group_2" + "&images=" + imgParam;
        }if(filePath.equals("/storage/emulated/0/PlayCamera/leidongliang.jpg")){*/
		
		// init
		//db.execSQL("INSERT INTO face (uid,picurl) values (1,'/storage/emulated/0/PlayCamera/dengying.jpg')");
		//db.execSQL("INSERT INTO face (uid,picurl) values (2,'/storage/emulated/0/PlayCamera/zhanglie.jpg')");
		//db.execSQL("INSERT INTO face (uid,picurl) values (3,'/storage/emulated/0/PlayCamera/chenqigang.jpg')");
		//db.execSQL("INSERT INTO face (uid,picurl) values (4,'/storage/emulated/0/PlayCamera/leidongliang.jpg')");
	}

	public boolean insert(ContentValues values) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			db.insert(TBL_NAME, null, values);
			db.close();
			return true;
		} catch (SQLException ex) {
			Log.d(TAG, "insert table failure");
			return false;
		}
	}
	
	public Cursor queryByPeopleCode(String peopleCode) {
		SQLiteDatabase db = getWritableDatabase();

		Cursor c = db.rawQuery("select * from face where peoplecode =?", new String[] { String.valueOf(peopleCode)});
		return c;
	}	
	
	public boolean updateByUid(int uid, ContentValues values) {
		try {
			if (db == null) {
				db = getWritableDatabase();
			}
			db.update(TBL_NAME, values, "uid=?", new String[] { String.valueOf(uid) });
			db.close();
			return true;
		} catch (SQLException ex) {
			Log.d(TAG, "update table failure");
			return false;
		}
	}
	
	public Cursor queryByUid(int uid) {
		SQLiteDatabase db = getWritableDatabase();
		//Cursor c = db.query(TBL_NAME, null, "number like ?", new String[] { "%"+String.valueOf(number)}, null, null, null);
		Cursor c = db.rawQuery("select * from face where uid =?", new String[] { String.valueOf(uid)});
		return c;
	}	
	
	public Cursor query() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TBL_NAME, null, null, null, null, null, "order by _id desc");
		return c;
	}
	
	public Cursor getPicUrlByNumber(String number) {
		SQLiteDatabase db = getWritableDatabase();
		//Cursor c = db.query(TBL_NAME, null, "number like ?", new String[] { "%"+String.valueOf(number)}, null, null, null);
		Cursor c = db.rawQuery("select * from MyRemote where number like ? order by _id desc", new String[] { "%"+String.valueOf(number)});
		return c;
	}		
	
	public boolean delByUid(int uid) {
		try {
			if (db == null) {
				db = getWritableDatabase();
			}
			db.delete(TBL_NAME, "uid=?", new String[] { String.valueOf(uid) });
			return true;
		} catch (SQLException ex) {
			Log.d(TAG, "delByUid failure");
			return false;
		}
	}

	public boolean delAll() {
		try {
			if (db == null) {
				db = getWritableDatabase();
			}
			
			db.execSQL("DELETE FROM face");
			
			return true;
		} catch (SQLException ex) {
			Log.d(TAG, "update table failure");
			return false;
		}
	}
	
	@Override
	public void close() {
		if (db != null)
			db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS diary");
		onCreate(db);
	}
}

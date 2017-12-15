package com.gi2t.face.detect.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloadFile {

	private Context mContext;

	/* 下载中 */
	private static final int DOWNLOAD = 1;

	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;

	/* 下载失败 */
	private static final int DOWNLOAD_FAIL = 3;

	public DownloadFile(Context context) {
		this.mContext = context;
	}

	/**
	 * 下载文件
	 */
	public void downloadFile() {
		new downloadFileThread().start();
	}

	/**
	 * 下载文件线程
	 */
	private class downloadFileThread extends Thread {

		@Override
		public void run() {
			downLoadFile("","","");
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD:
				break;

			case DOWNLOAD_FINISH:
				Log.e("dengyingUpdate", "DOWNLOAD_FINISH");

				break;

			case DOWNLOAD_FAIL:
				Log.e("dengyingUpdate", "DOWNLOAD_FAIL");
				
				break;
			default:
				break;
			}
		};
	};

	
	private void downLoadFile(String mDownLoadUrl,String mSavePath,String mSaveFileName){
		try {
			URL url = new URL(mDownLoadUrl);

			// 创建连接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();

			// 获取文件大小
			int length = conn.getContentLength();

			// 创建输入流
			InputStream is = conn.getInputStream();

			File filePatch = new File(mSavePath);

			// 判断文件目录是否存在
			if (!filePatch.exists()) {
				filePatch.mkdirs();

				// mkdir是创建目录，只是创建单击目录，
				// 而且必须是已经存在的目录下创建目录。

				// mkdirs可以创建多级目录，
				// 可以在不存在的目录下创建多级目录
			}
			File file = new File(mSavePath, mSaveFileName);

			FileOutputStream fos = new FileOutputStream(file);

			// 缓存
			byte buf[] = new byte[1024];

			// 写入到文件中
			do {
				int numread = is.read(buf);

				if (numread <= 0) {
					//下载完成
					mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
					break;
				}

				// 写入文件
				fos.write(buf, 0, numread);
			} while (true);

			fos.close();
			is.close();
		} catch (Exception e) {
			Log.e("dengying", "Exception=" + e.toString());

			e.printStackTrace();
		} 
	}
}

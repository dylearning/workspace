package com.gi2t.tdmec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import android.util.Log;

public class MyClient {
	//public static String HOST = "192.168.14.155";
	//public static String HOST = "192.168.1.9";
	//public static int PORT = 9776;
	
	public static String HOST = "101.231.241.28";
	public static int PORT = 9704;

	private static boolean isConnected = false;

	public static void init() {

		NioEventLoopGroup group = new NioEventLoopGroup();
		try {

			Bootstrap bootstrap = new Bootstrap();

			bootstrap.channel(NioSocketChannel.class);

			bootstrap.handler(new MyClientInitializer());

			bootstrap.group(group);

			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

			Log.e("dengying","init HOST="+HOST+",PORT="+PORT);
			
			ChannelFuture future = bootstrap.connect(new InetSocketAddress(HOST, PORT));
			future.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture f) throws Exception {
					if (f.isSuccess()) {
						Log.e("dengying", "MyClient Started Tcp Client Success! ");
					} else {
						Log.e("dengying", "MyClient Started Tcp Client Failed!");
						new Thread(new Runnable() {
							@Override
							public void run() {					
								doConnect();// 连接服务器
							}
						}).start();
					}
				}
			});

		} catch (Exception e) {

			Log.e("dengying", "MyClient connected e=" + e.toString());
			e.printStackTrace();
		}
	}

	private static void doConnect() {

		Log.e("dengying", "MyClient doConnect isConnected=" + isConnected);

		if (isConnected) {
			return;
		}

		NioEventLoopGroup group = new NioEventLoopGroup();
		try {

			Bootstrap bootstrap = new Bootstrap();

			bootstrap.channel(NioSocketChannel.class);

			bootstrap.handler(new MyClientInitializer());

			bootstrap.group(group);

			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

			Log.e("dengying","doConnect HOST="+HOST+",PORT="+PORT);
			
			ChannelFuture future = bootstrap.connect(new InetSocketAddress(HOST, PORT));
			future.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture f) throws Exception {
					if (f.isSuccess()) {
						isConnected = true;

						Log.e("dengying", "MyClient Started Tcp Client Success!");
					} else {
						isConnected = false;
						Thread.sleep(1000*3);
						doConnect();
						Log.e("dengying", "MyClient Started Tcp Client Failed!");
					}
				}
			});

		} catch (Exception e) {

			Log.e("dengying", "MyClient connected e=" + e.toString());
			e.printStackTrace();
		}
	}
}
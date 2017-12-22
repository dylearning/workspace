package com.gi2t.tdmec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Log;
  

public class MyClientThread extends Thread {  
   
	private static final String TAG = "dengying";
	
    //public static String HOST = "192.168.14.155"; 
    public static String HOST = "192.168.1.9";
    public static int PORT = 9776;  
    
	private boolean isConnected = false;
	
	private ScheduledExecutorService mScheduledExecutorService;
	
    public void run() {
    	
    	//doConnect();
    	
    	NioEventLoopGroup group = new NioEventLoopGroup();
		try {

			// Client服务启动器 3.x的ClientBootstrap
			// 改为Bootstrap，且构造函数变化很大，这里用无参构造。
			Bootstrap bootstrap = new Bootstrap();
			// 指定channel类型
			bootstrap.channel(NioSocketChannel.class);
			// 指定Handler
			bootstrap.handler(new MyClientInitializer());
			// 指定EventLoopGroup
			bootstrap.group(group);
				
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			
			ChannelFuture future = bootstrap.connect(new InetSocketAddress(HOST, PORT));
	   		future.addListener(new ChannelFutureListener() {
	   			public void operationComplete(ChannelFuture f) throws Exception {
	   				if (f.isSuccess()) {
	   					mScheduledExecutorService.shutdown();
	   					
	   					isConnected = true;
	   					
	   					Log.e("dengying","Started Tcp Client Success: ");
	   				} else {
	   					
	   					isConnected = false;
	   					
	   					Log.e("dengying","Started Tcp Client Failed: ");
	   									
	   					// 自定义心跳，每隔3秒向服务器发送重连
	   					/*mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	   			        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
	   			            @Override
	   			            public void run() {
	   			            	doConnect();
	   			            }
	   			        }, 10, 3, TimeUnit.SECONDS);*/
	   				}
	   			}
	   		});
	        
		} catch (Exception e) {
			
			Log.e("dengying","MyClientThread connected e="+e.toString());
			e.printStackTrace();
		}
	}  
    
	private void doConnect() {
		
		Log.e("dengying", "doConnect isConnected=" + isConnected);
		
		if (isConnected) {
			return;
		}
		
    	NioEventLoopGroup group = new NioEventLoopGroup();
		try {

			// Client服务启动器 3.x的ClientBootstrap
			// 改为Bootstrap，且构造函数变化很大，这里用无参构造。
			Bootstrap bootstrap = new Bootstrap();
			// 指定channel类型
			bootstrap.channel(NioSocketChannel.class);
			// 指定Handler
			bootstrap.handler(new MyClientInitializer());
			// 指定EventLoopGroup
			bootstrap.group(group);
				
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			
			ChannelFuture future = bootstrap.connect(new InetSocketAddress(HOST, PORT));
	   		future.addListener(new ChannelFutureListener() {
	   			public void operationComplete(ChannelFuture f) throws Exception {
	   				if (f.isSuccess()) {
	   					mScheduledExecutorService.shutdown();
	   					
	   					isConnected = true;
	   					
	   					Log.e("dengying","Started Tcp Client Success: ");
	   				} else {
	   					isConnected = false;
	   					
	   					Log.e("dengying","Started Tcp Client Failed: ");
	   									
	   					// 自定义心跳，每隔3秒向服务器发送重连
	   					mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	   			        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
	   			            @Override
	   			            public void run() {
	   			            	doConnect();
	   			            }
	   			        }, 10, 3, TimeUnit.SECONDS);
	   				}
	   			}
	   		});
	        
		} catch (Exception e) {
			
			Log.e("dengying","MyClientThread connected e="+e.toString());
			e.printStackTrace();
		}
	}
}  
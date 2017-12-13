package org.jan.nio.exapmle;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import android.util.Log;
  

public class MyClientThread extends Thread {  
   
	private static final String TAG = "dengying";
	
    public static String HOST = "192.168.14.155";  
    public static int PORT = 9776;  
    
    public void run() {
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
	   					Log.e("dengying","Started Tcp Client Success: ");
	   				} else {
	   					Log.e("dengying","Started Tcp Client Failed: ");
	   					// 3秒重连
	   					//f.channel().eventLoop().schedule(() -> doConnect(), 3, TimeUnit.SECONDS);
	   				}
	   			}
	   		});
	        
		} catch (Exception e) {
			
			Log.e("dengying","MyClientThread connected e="+e.toString());
			e.printStackTrace();
		}
	}  
}  
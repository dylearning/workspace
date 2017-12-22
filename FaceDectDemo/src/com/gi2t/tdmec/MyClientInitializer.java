package com.gi2t.tdmec;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import android.util.Log;

public class MyClientInitializer extends ChannelInitializer<SocketChannel> {

	private boolean isConnected = false;
	//public static String HOST = "192.168.14.155";
	public static String HOST = "192.168.1.9";
	public static int PORT = 9776;
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
		pipeline.addFirst(new ChannelInboundHandlerAdapter() {
			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				super.channelInactive(ctx);
				// 3秒重连
				//ctx.channel().eventLoop().schedule(() -> doConnect(), 3, TimeUnit.SECONDS);
				
				Log.e("dengying","MyClientInitializer doConnect");
				
				new Thread(new Runnable() {
					@Override
					public void run() {					
						doConnect();// 连接服务器
					}
				}).start();
			}
		});
		
		/**
		 * 这个地方的必须和服务端对应上。否则无法正常解码和编码
		 */
//		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));//分包
//		pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
//		pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());
		
		//客户端的逻辑
		pipeline.addLast("handler",new MyClientHandler(1, HOST, PORT));
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		Log.e("dengying","MyClientInitializer channelRead");
		
		super.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	
		Log.e("dengying","MyClientInitializer channelReadComplete");
		
		super.channelReadComplete(ctx);
	}
	
	
	private  void doConnect() {

		Log.e("dengying", "MyClientInitializer doConnect isConnected=" + isConnected);

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

			ChannelFuture future = bootstrap.connect(new InetSocketAddress(HOST, PORT));
			future.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture f) throws Exception {
					if (f.isSuccess()) {
						isConnected = true;

						Log.e("dengying", "Started Tcp Client Success: ");
					} else {
						isConnected = false;
						Thread.sleep(1000*3);
						doConnect();
						Log.e("dengying", "Started Tcp Client Failed: ");
					}
				}
			});

		} catch (Exception e) {

			Log.e("dengying", "MyClientThread connected e=" + e.toString());
			e.printStackTrace();
		}
	}

}

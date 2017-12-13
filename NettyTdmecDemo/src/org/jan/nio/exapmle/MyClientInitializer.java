package org.jan.nio.exapmle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import android.util.Log;

public class MyClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		/**
		 * ����ط��ı���ͷ���˶�Ӧ�ϡ������޷���������ͱ���
		 */
//		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));//�ְ�
//		pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
//		pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast("decoder", new MessageDecoder());
		pipeline.addLast("encoder", new MessageEncoder());
		
		//�ͻ��˵��߼�
		pipeline.addLast("handler",new MyClientHandler(1, "127.0.0.1", 111));
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
	
}

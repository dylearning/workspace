package org.jan.nio.exapmle;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class MyClientHandler extends SimpleChannelInboundHandler<TdmecMessage> {

	private int hubId;

	private int sendNo;

	private String serverIp;

	private int serverPort;

	private int testConNum;

	public MyClientHandler(int hubId, String serverIp, int serverPort) {
		Log.e("dengying","MyClientHandler");
		
		this.hubId = hubId;
		this.serverIp = serverIp;
		this.serverPort = serverPort;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TdmecMessage msg) throws Exception {
		//logger.info("Agent client channelRead0:" + JSON.toJSONString(msg));
		if (msg.getFid() == 0x00 && msg.getCid() == 0x00) { // 窗口确认回复
			sendNo = msg.getSseq();
		} else if ((msg.getFid() & 0xff) == 0x02 && (msg.getCid() & 0xff) == 0x80) {
			
			//上报设备信息
    		ByteBuf byteBuf = Unpooled.buffer(1024);
	        byteBuf.writeShort(ByteUtil.changeByte((short) 1)); //设备数量
	        byteBuf.writeShort(ByteUtil.changeByte((short) 0)); //设备类型 
	        byteBuf.writeShort(ByteUtil.changeByte((short) 1)); //设备地址
	        byteBuf.writeByte((byte) 0); //设备联脱网状态
	        byteBuf.writeLong(ByteUtil.changeByte(8888L));//设备SN
	        byte[] content = new byte[byteBuf.writerIndex()];
			byteBuf.readBytes(content);
			
    		TdmecMessage message = new TdmecMessage((byte)0X03, (byte) 0X00, content);
    		ctx.writeAndFlush(message);
		}
	
		Log.e("dengying","MyClientHandler channelRead0 msg=" + msg.toString()+",fid="+msg.getFid()+",cid="+msg.getCid());

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//logger.info("Agent client active! id:" + ctx.channel().id().asShortText());
		super.channelActive(ctx);
		
		Log.e("dengying","MyClientHandler channelActive");
		
		
		// 自定义心跳，每隔10秒向服务器发送心跳包
		final ChannelHandlerContext mChannelHandlerContext = ctx;
		ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
            	Log.e("dengying","TdmecMessage heart beat");
            	
        		TdmecMessage message = new TdmecMessage((byte)0XFF, (byte) 0X00, new byte[0]);
        		
        		mChannelHandlerContext.writeAndFlush(message);
            }
        }, 20, 10, TimeUnit.SECONDS);
        
        
        //登录
        ByteBuf byteBuf = Unpooled.buffer(1024);
        byteBuf.writeByte((byte) 1); //VERH
        byteBuf.writeByte((byte) 2); //VERH
        byteBuf.writeLong(ByteUtil.changeByte(1L)); //SITEID
        byteBuf.writeLong(ByteUtil.changeByte(8888L));//SN
        byteBuf.writeBytes("0123456789".getBytes());//PWD
        byteBuf.writerIndex(50);
        
        String o = "hardVer:1,softVer:2";
        byteBuf.writeShort(o.getBytes().length); //INFO LEN
        byteBuf.writeBytes(o.getBytes());        //INFO
        
        byte[] content = new byte[byteBuf.writerIndex()];
		byteBuf.readBytes(content);
		
		TdmecMessage message = new TdmecMessage((byte)0X02, (byte) 0X00, content);
		ctx.writeAndFlush(message);
	}

	/**
	 * 客户端超时处理
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.ALL_IDLE) {
				if (testConNum > 3) { // 通信检测三次后无返回,断开连接
					channelInactive(ctx);
				} else {
					// 超时后, 发送通信检测帧
				}
			}
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//logger.error("Agent client close");
		super.channelInactive(ctx);
	}
}
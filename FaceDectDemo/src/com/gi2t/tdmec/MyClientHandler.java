package com.gi2t.tdmec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.ai.aip.demo.FaceAdd;
import com.baidu.ai.aip.demo.FaceDelete;
import com.gi2t.face.detect.activity.MyApplication;
import com.gi2t.face.detect.util.FileUtil;
import com.gi2t.face.detect.util.ImageUtil;

public class MyClientHandler extends SimpleChannelInboundHandler<TdmecMessage> {
	
	public static ChannelHandlerContext mChannelHandlerContext;
	
	private ScheduledExecutorService mScheduledExecutorService;
	
	private int hubId;

	private int sendNo;

	private String serverIp;

	private int serverPort;

	private int testConNum;
	
	private Map<String, JSONObject> photoMap = new HashMap<String, JSONObject>();

	public MyClientHandler(int hubId, String serverIp, int serverPort) {
//		Log.e("dengying","MyClientHandler");
		
		this.hubId = hubId;
		this.serverIp = serverIp;
		this.serverPort = serverPort;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TdmecMessage msg) throws Exception {

		mChannelHandlerContext = ctx;
		
		//登陆成功，上报设备信息
		if ((msg.getFid() & 0xff) == 0x02 && (msg.getCid() & 0xff) == 0x80) {
			
			Log.e("dengying", "channelRead0 upload devices info");
			
			//上报设备信息
    		ByteBuf byteBuf = Unpooled.buffer(1024);
	        byteBuf.writeShort(ByteUtil.changeByte((short) 1)); //设备数量
	        byteBuf.writeShort(ByteUtil.changeByte((short) 0)); //设备类型 
	        byteBuf.writeShort(ByteUtil.changeByte((short) 1)); //设备地址
	        byteBuf.writeByte((byte) 0); //设备联脱网状态
	        byteBuf.writeLong(ByteUtil.changeByte(88881001L));//设备SN
	        byte[] content = new byte[byteBuf.writerIndex()];
			byteBuf.readBytes(content);
			
    		TdmecMessage message = new TdmecMessage((byte)0X03, (byte) 0X00, content);
    		ctx.writeAndFlush(message);
		} else if ((msg.getFid() & 0xff) == 0x04 && (msg.getCid() & 0xff) == 0x00) { // 自定义帧  // 布控  //取消布控
			try {
				ByteBuf bmByteBuf = Unpooled.copiedBuffer(msg.getBmByteContent());
				short len = ByteUtil.changeByte(bmByteBuf.readShort());// 长度
				byte[] byteInfo = new byte[len];
				bmByteBuf.readBytes(byteInfo);
				String info = new String(byteInfo);
				//System.out.println("info:" + JSON.parseObject(info).toJSONString());
				
				Log.e("dengying", "do face:info="+JSON.parseObject(info).toJSONString());
				
				short msgNo = ByteUtil.changeByte(bmByteBuf.readShort());// msgNo
				// 类型
				String type = JSON.parseObject(info).getString("type");
	        	
	        	if ("1000".equals(type)) { // 布控
	        		// 总帧数
					int frameCnt = JSON.parseObject(info).getInteger("frameCnt");
					// 当前帧数
		        	int currentCnt = JSON.parseObject(info).getInteger("currentCnt");
		        	String photo = JSON.parseObject(info).getString("Photo2");
		        	String deviceCode = JSON.parseObject(info).getString("deviceCode");
		        	
		        	String ID_Code =((JSONObject) JSON.parseObject(info).get("ID_Info")).getString("ID_Code");
		        	String Name = ((JSONObject) JSON.parseObject(info).get("ID_Info")).getString("Name");
		        	String Sex = ((JSONObject) JSON.parseObject(info).get("ID_Info")).getString("Sex");
		        		
	        		if (photoMap.containsKey(deviceCode)) {
		        		String temp = photoMap.get(deviceCode).getString("Photo2");
		        		photo = temp + photo;
		        		photoMap.get(deviceCode).put("Photo2", photo);
		        	} else {
		        		photoMap.put(deviceCode, JSON.parseObject(info));
		        	}
	        		
	        		int uid = -1;
	        	        		
	        		if (frameCnt == currentCnt) {
	        			// 图片
	        			Log.e("dengying", "do face:ID_Code="+ID_Code+",Name="+Name+",Sex="+Sex);
	        			
	        			//Log.e("dengying", photoMap.get(deviceCode).getString("Photo2"));
	        			
	        			boolean saveImag = FileUtil.saveBitmap(ImageUtil.stringtoBitmap(photoMap.get(deviceCode).getString("Photo2")), ID_Code);
	        			
	        			Log.e("dengying", "do face:saveImag ="+saveImag);
	        			
	        			photoMap.remove(deviceCode);
	        			
	        			uid = FaceAdd.add(MyApplication.getContext(),ID_Code,Name,Sex,"/storage/emulated/0/PlayCamera/"+ID_Code+".jpg");
	        		
	        			Log.e("dengying", "do face:FaceAdd.add uid ="+uid);
	        		}
	        		
	        		ByteBuf byteBuf = Unpooled.buffer(1024);
					JSONObject json = new JSONObject();
					JSONObject Result = new JSONObject();
					
					if(uid>0){
						Result.put("ResultCode", 0);
					}else{
						Result.put("ResultCode", 1);
					}
					Result.put("ResultText", "");
					JSONObject Person = new JSONObject();
					Person.put("ID_Code", ID_Code);
					Person.put("Person_ID", uid);
					
					json.put("Result", Result);
					json.put("Person", Person);
					byteBuf.writeShort(ByteUtil.changeByte((short)json.toJSONString().getBytes().length)); //长度
					byteBuf.writeBytes(json.toJSONString().getBytes());
					byteBuf.writeShort(ByteUtil.changeByte(msgNo)); //msgNo
					byte[] content = new byte[byteBuf.writerIndex()];
					byteBuf.readBytes(content);
					
					TdmecMessage message = new TdmecMessage((byte)0X04, (byte) 0X80, content);
					ctx.writeAndFlush(message);
					byteBuf.release();
	        	} else if ("1001".equals(type)) { // 取消布控
		        	String Person_Code = JSON.parseObject(info).getString("Person_Code");
		        	String Person_ID = JSON.parseObject(info).getString("Person_ID");
		        	String deviceCode = JSON.parseObject(info).getString("deviceCode");
		        	
		        	Log.e("dengying", "cancel Face:Person_Code="+Person_Code+",Person_ID="+Person_ID+",deviceCode="+deviceCode);
		        	
		        	boolean ret = FaceDelete.delete(Integer.parseInt(Person_ID));
		        	
	        		ByteBuf byteBuf = Unpooled.buffer(1024);
					JSONObject json = new JSONObject();
					JSONObject Result = new JSONObject();
					
					if(ret){
						Result.put("ResultCode", 0);
					}else{
						Result.put("ResultCode", 1);
					}
					Result.put("ResultText", "");
	
					json.put("Result", Result);
			
					byteBuf.writeShort(ByteUtil.changeByte((short)json.toJSONString().getBytes().length)); //长度
					byteBuf.writeBytes(json.toJSONString().getBytes());
					byteBuf.writeShort(ByteUtil.changeByte(msgNo)); //msgNo
					byte[] content = new byte[byteBuf.writerIndex()];
					byteBuf.readBytes(content);
					
					TdmecMessage message = new TdmecMessage((byte)0X04, (byte) 0X80, content);
					ctx.writeAndFlush(message);
					byteBuf.release();
	        	}
				
				// 释放bytebuf
				bmByteBuf.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ((msg.getFid() & 0xff) == 0x04 && (msg.getCid() & 0xff) == 0x01) { // 单点调控 开锁
			try {
				ByteBuf bmByteBuf = Unpooled.copiedBuffer(msg.getBmByteContent());
				short addr = ByteUtil.changeByte(bmByteBuf.readShort()); // 设备地址
				short num = ByteUtil.changeByte(bmByteBuf.readShort());// 测点数量
				Byte pipeType = bmByteBuf.readByte();// 类型
				short pipe = ByteUtil.changeByte(bmByteBuf.readShort());// 测点地址
				Object object = ByteUtil.parseChValueByChType(pipeType, bmByteBuf);// 测点值
				if (pipe == 8192 && "1".equals(object.toString())) {
					
					// 一键开门
					ByteBuf byteBuf = Unpooled.buffer(1024);
					byteBuf.writeBytes(msg.getBmByteContent());
					
					// 结果
					byteBuf.writeByte(Byte.parseByte("0"));
					byteBuf.writeByte(Byte.parseByte("0"));
					
					byte[] content = new byte[byteBuf.writerIndex()];
					byteBuf.readBytes(content);
					
					Log.e("dengying", "open door");
					
					TdmecMessage message = new TdmecMessage((byte)0X04, (byte) 0X81, content);
					ctx.writeAndFlush(message);
					
					// 释放bytebuf
					byteBuf.release();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		//Log.e("dengying","MyClientHandler channelRead0 msg=" + msg.toString()+",fid="+msg.getFid()+",cid="+msg.getCid());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//logger.info("Agent client active! id:" + ctx.channel().id().asShortText());
		super.channelActive(ctx);
		
		Log.e("dengying","MyClientHandler channelActive");
			
		// 自定义心跳，每隔10秒向服务器发送心跳包
		final ChannelHandlerContext mChannelHandlerContext = ctx;
		if(mScheduledExecutorService == null){
			mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		}else{
			mScheduledExecutorService.shutdown();
		}
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
        byteBuf.writeLong(ByteUtil.changeByte(88881001L));//SN
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
package org.jan.nio.exapmle;

public class TdmecMessage{

	private static final long serialVersionUID = 1L;
	// private Map<String, Integer> pingNumManager = new
	// ConcurrentHashMap<String, Integer>();
	private static Short pingNum = 0;
	// 消息SOI
	public static final byte MESSAGE_START = (byte) 0X6B;

	// 通信检测帧
	public static final byte FID_SIGNAL = (byte) 0XFF;
	// hub 信息帧
	public static final byte FID_HUB = (byte) 0XF1;
	// 配置帧
	public static final byte FID_CONFIG = (byte) 0XF0;

	// 通信检测
	public static final byte CID_SIGNAL = (byte) 0X00;
	// 通信检测
	public static final byte CID_SIGNAL_RESPONSE = (byte) 0X80;

	// 请求hub sn
	public static final byte CID_HUB_SN = (byte) 0X00;
	// 联网
	public static final byte CID_CONNECT = (byte) 0X02;
	// ec控制
	public static final byte CID_CONTROL = (byte) 0X08;
	// 导入所有ec连接信息
	public static final byte CID_EC_CONN_INFO_ALL = (byte) 0X10;
	// 导入单个ec连接信息
	public static final byte CID_EC_CONN_INFO = (byte) 0X11;
	// ec上报数据
	public static final byte CID_UPLOAD_DATA = (byte) 0X19;
	// ec上报数据确认
	public static final byte CID_UPLOAD_DATA_CONFIRM = (byte) 0X99;
	// 设备通信状态
	public static final byte CID_DEVICE_STATE = (byte) 0X21;
	// 设备通信状态确认
	public static final byte CID_DEVICE_CONFIRM = (byte) 0XA1;
	// 联网确认帧
	public static final byte CID_CONN_CONFIRM = (byte) 0X82;
	// ec脱网确认帧
	public static final byte CID_OFF_NET_CONFIRM = (byte) 0X83;
	// 遥控确认帧
	public static final byte CID_CONTROL_CONFIRM = (byte) 0X88;

	// EC自定义消息帧
	public static final byte CID_CUSTOM = (byte) 0X2A;
	// EC自定义消息帧确认回复
	public static final byte CID_CUSTOM_CONFIRM = (byte) 0XAA;

	// 自定义消息帧
	public static final byte CID_CUSTOM_ = (byte) 0X26;
	// 自定义消息帧确认回复
	public static final byte CID_CUSTOM_CONFIRM_ = (byte) 0XA6;

	// 单点调控控制
	public static final byte CID_SINGLE_CONTROL_SN = (byte) 0X31;
	// 单点调控控制确认
	public static final byte CID_SINGLE_CONTROL_CONFIRM = (byte) 0XB1;

	// 导入EC断开信息
	public static final byte CID_EC_OFF = (byte) 0X12;
	// 导入EC断开信息确认
	public static final byte CID_EC_OFF_CONFIRM = (byte) 0X92;

	public static final int TYPE_TCP = 0;
	public static final int TYPE_MQTT = 1;

	private byte bmStart;
	private byte fid;
	private byte cid;
	private byte sseq;
	private short bmlen;
	// private String bmContent;
	// 处理内存溢出的问题,避免直接使用ByteBuf，因为需要进行关闭操作
	// private ByteBuf bmByteContent;
	private byte[] bmByteContent;
	// private ChannelHandlerContext chandlerContext;
	private byte verH;
	private byte verL;
	private int transType;
	private Long pSn;

	public Long getpSn() {
		return pSn;
	}

	public void setpSn(Long pSn) {
		this.pSn = pSn;
	}

	public int getTransType() {
		return transType;
	}

	public void setTransType(int transType) {
		this.transType = transType;
	}

	public TdmecMessage() {
	}

	public TdmecMessage(byte fid, byte cid, byte sseq, short bmlen, byte[] bmByteContent) {
		this.bmStart = 0X6B;
		this.fid = fid;
		this.cid = cid;
		this.sseq = sseq;
		// this.bmlen = bmlen;
		setBmlen(bmlen);
		this.bmByteContent = bmByteContent;
	}

	public TdmecMessage(byte fid, byte cid, byte sseq, byte[] bmByteContent) {
		this.bmStart = 0X6B;
		this.fid = fid;
		this.cid = cid;
		this.sseq = sseq;
		// this.bmlen = (short)bmByteContent.readableBytes();
		setBmlen((short) bmByteContent.length);
		this.bmByteContent = bmByteContent;
	}

	public TdmecMessage(byte fid, byte cid, byte[] bmByteContent) {
		this.bmStart = 0X6B;
		this.fid = fid;
		this.cid = cid;
		this.sseq = (byte) 0;
		short len = (short) (bmByteContent.length);
		// this.bmlen = len;
		setBmlen(len);
		this.bmByteContent = bmByteContent;
	}

	public byte getBmStart() {
		return bmStart;
	}

	public void setBmStart(byte bmStart) {
		this.bmStart = bmStart;
	}

	public byte getFid() {
		return fid;
	}

	public void setFid(byte fid) {
		this.fid = fid;
	}

	public byte getCid() {
		return cid;
	}

	public void setCid(byte cid) {
		this.cid = cid;
	}

	public byte getSseq() {
		return sseq;
	}

	public void setSseq(byte sseq) {
		this.sseq = sseq;
	}

	public short getBmlen() {
		return bmlen;
	}

	public void setBmlen(short bmlen) {
		this.bmlen = bmlen;
	}

	public byte[] getBmByteContent() {
		return bmByteContent;
	}

	public void setBmByteContent(byte[] bmByteContent) {
		this.bmByteContent = bmByteContent;
	}

	// public ChannelHandlerContext getChandlerContext() {
	// return chandlerContext;
	// }
	//
	// public void setChandlerContext(ChannelHandlerContext chandlerContext) {
	// this.chandlerContext = chandlerContext;
	// }

	public byte getVerH() {
		return verH;
	}

	public void setVerH(byte verH) {
		this.verH = verH;
	}

	public byte getVerL() {
		return verL;
	}

	public void setVerL(byte verL) {
		this.verL = verL;
	}

	public static short getBMNumber() {
		if (pingNum >= Short.MAX_VALUE) {
			pingNum = 0;
		}
		pingNum++;
		return pingNum;
	}

	// 重写toString()方法
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("TDMECMessage:");
		buffer.append(" start:").append("0x").append(Integer.toHexString(bmStart & 0xff));
		buffer.append(" fid:").append("0x").append(Integer.toHexString(fid & 0xff));
		buffer.append(" cid:").append("0x").append(Integer.toHexString(cid & 0xff));
		buffer.append(" infoLen:").append("0x").append(Integer.toHexString(bmlen & 0xff));
		//buffer.append(" infoByte:").append(DataConUtil.printHexString(bmByteContent));
		return buffer.toString();
	}
}

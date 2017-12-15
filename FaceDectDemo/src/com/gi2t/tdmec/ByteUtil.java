package com.gi2t.tdmec;

//import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtil {
	private static byte[] shortToByteArray(short s) {
		byte[] shortBuf = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (shortBuf.length - 1 - i) * 8;
			shortBuf[i] = (byte) ((s >>> offset) & 0xff);
		}
		return shortBuf;
	}

	public static short changeByte(short in) {
		byte[] bs = shortToByteArray(in);
		ByteBuffer buffer2 = ByteBuffer.wrap(bs);
		buffer2.order(ByteOrder.LITTLE_ENDIAN);
		return buffer2.getShort();
	}

	public static int changeByte(int in) {
		byte[] bs = Utility.int2Byte(in);
		ByteBuffer buffer2 = ByteBuffer.wrap(bs);
		buffer2.order(ByteOrder.LITTLE_ENDIAN);
		return buffer2.getInt();
	}

	public static long changeByte(long in) {
		byte[] bs = Utility.long2Byte(in);
		ByteBuffer buffer2 = ByteBuffer.wrap(bs);
		buffer2.order(ByteOrder.LITTLE_ENDIAN);
		return buffer2.getLong();
	}

	/**
	 * 解析测点值
	 * 
	 * @param chType
	 * @param bmByteBuf
	 */
	public static Object parseChValueByChType(Byte chType, ByteBuf bmByteBuf) {
		CppInputStream st = new CppInputStream(new ByteBufInputStream(bmByteBuf));
		Object object = null;
		try {
			switch (chType & 0xff) {
			case 11:// BYTE数据(CHTYPE=11)
				object = st.readByte() & 0xff;
				break;
			case 12:// FLOAT数据(CHTYPE=12)
				object = st.readFloat();
				break;
			case 13:// DOUBLE数据(CHTYPE=13)
				object = st.readDouble();
				break;
			case 14:// Word数据(CHTYPE=14)
				object = st.readShort() & 0xffff;
				break;
			case 15:// UINT32数据(CHTYPE=15)
				object = st.readInt();
				break;
			case 16:// INT数据(CHTYPE=16)
				object = st.readInt();
				break;
			case 17:// INT64数据(CHTYPE=17)
				object = st.readLong();
				break;
			case 18:// UINT64数据(CHTYPE=18)
				object = st.readLong();
				break;
			case 19:// string数据(CHTYPE=19)
				int len = st.readUnsignedShort();
				byte[] bytes = new byte[len];
				st.read(bytes, 0, len);
				String string = new String(bytes);
				object = string;
				break;


			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

}

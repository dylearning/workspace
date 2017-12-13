package org.jan.nio.exapmle;

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
	
}

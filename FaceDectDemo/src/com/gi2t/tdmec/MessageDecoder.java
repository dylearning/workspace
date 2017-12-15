package com.gi2t.tdmec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;


import java.util.List;

/**
 * Created by Thomas.Wang on 2017/3/2.
 */
public class MessageDecoder extends ByteToMessageDecoder {
    //private final static Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    public MessageDecoder() {

    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        if (in.readableBytes() >= 6) {
        	in.markReaderIndex();
            TdmecMessage bm = new TdmecMessage();
            final CppInputStream st = new CppInputStream(new ByteBufInputStream(in));
            byte soi = in.readByte();
            if(soi == 0x6B){
                byte fid = in.readByte();
                byte cid = in.readByte();
                byte sseq = in.readByte();
                short len = ByteUtil.changeByte(in.readShort());
                bm.setBmStart(soi);
                bm.setFid(fid);
                bm.setCid(cid);
                bm.setSseq(sseq);
                bm.setBmlen(len);
                if (len <= st.available()) {
                	byte[] buf = new byte[len];
                    in.readBytes(buf);
                    bm.setBmByteContent(buf);
					list.add(bm);
					//logger.info("messageReceived:" + "soi:" + (soi & 0xff) + "fid:" + (fid& 0xff) + "cid:" + (cid& 0xff) + "infoLen:" + (len& 0xffff));
				} else {
					st.reset();
					return;
				}
            } else {
            	in.resetReaderIndex();
				return;
			}
        }
    }
}

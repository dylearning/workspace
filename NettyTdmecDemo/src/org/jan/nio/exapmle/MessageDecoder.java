package org.jan.nio.exapmle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        if (in.readableBytes() >= 6) {
            TdmecMessage bm = new TdmecMessage();
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
                if(len >= 1) {
                	byte[] buf = new byte[len];
                    in.readBytes(buf);
                    bm.setBmByteContent(buf);
                }
                list.add(bm);
            }
        }
    }
}

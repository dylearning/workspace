package org.jan.nio.exapmle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<TdmecMessage>{

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, TdmecMessage msg, ByteBuf out) throws Exception {

        out.writeByte(msg.getBmStart());
        out.writeByte(msg.getFid());
        out.writeByte(msg.getCid());
        out.writeByte(msg.getSseq());
        short blen = msg.getBmlen();
        out.writeShort(ByteUtil.changeByte(blen));
        if (blen >= 1) {
            ByteBuf buf = Unpooled.buffer(blen);
            buf.writeBytes(msg.getBmByteContent());
            out.writeBytes(buf);
        }
    }
}

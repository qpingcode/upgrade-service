package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static me.qping.upgrade.common.constant.ServerConstant.MSG_PROTOCAL_CHARSET;
import static me.qping.upgrade.common.constant.ServerConstant.MSG_PROTOCAL_ID;

/**
 * 编码器
 * @author admin
 *
 */
public class ObjEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out)  {
        byte[] data = Serialization.serialize(in);
        // 写入协议开头
        out.writeCharSequence(MSG_PROTOCAL_ID, MSG_PROTOCAL_CHARSET);
        // 写入消息类型
        out.writeByte(Serialization.getMsgType(in.getClass()));
        // 写入消息
        out.writeBytes(data);
    }

}

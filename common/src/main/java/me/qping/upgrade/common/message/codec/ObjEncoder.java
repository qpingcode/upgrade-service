package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.qping.upgrade.common.message.Msg;

import java.nio.charset.Charset;

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
        out.writeCharSequence(MSG_PROTOCAL_ID, Charset.forName("utf8"));
        out.writeByte(Serialization.getMsgType(in.getClass()));
        out.writeBytes(data);
    }

}

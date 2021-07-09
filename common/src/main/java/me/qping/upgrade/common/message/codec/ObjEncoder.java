package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.qping.upgrade.common.message.Msg;

/**
 * 编码器
 * @author admin
 *
 */
public class ObjEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;

    public ObjEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out)  {
        byte[] data = Serialization.serialize(in);
//        Msg msg = (Msg)in;
//        out.writeByte(msg.getType());
        out.writeBytes(data);
    }

}

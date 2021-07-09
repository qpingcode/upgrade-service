package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码器
 * @author admin
 *
 */
public class ObjDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public ObjDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
//        byte msgType = in.readByte();
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);
        out.add(Serialization.deserialize(data, genericClass));
    }

}

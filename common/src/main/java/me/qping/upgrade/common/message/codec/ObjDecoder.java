package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

import static me.qping.upgrade.common.constant.ServerConstant.MSG_PROTOCAL_ID;

/**
 * 解码器
 * @author admin
 *
 */
public class ObjDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        CharSequence msgProtocolID = in.readCharSequence(2, Charset.forName("utf8"));
        if(!msgProtocolID.equals(MSG_PROTOCAL_ID)){
            throw new Exception("消息非法");
        }

        byte msgType = in.readByte();
        Class<?> genericClass = Serialization.getClass(msgType);
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);
        out.add(Serialization.deserialize(data, genericClass));
    }

}

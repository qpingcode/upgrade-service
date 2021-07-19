package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.qping.upgrade.common.constant.MsgType;

import java.util.List;

import static me.qping.upgrade.common.constant.ServerConstant.*;

/**
 * 解码器
 * @author admin
 *
 */
public class ObjDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 读取并判断协议开头
        CharSequence msgProtocolID = in.readCharSequence(MSG_PROTOCAL_ID_LENGTH, MSG_PROTOCAL_CHARSET);
        if(!msgProtocolID.equals(MSG_PROTOCAL_ID)){
            throw new Exception("消息非法");
        }

        // 读取消息类型
        byte msgType = in.readByte();

        if(msgType == MsgType.REGISTER.val()){
            System.out.println("zhuce");
        }

        Class<?> genericClass = Serialization.getClass(msgType);
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);

        // 根据消息类型将消息转为对象
        out.add(Serialization.deserialize(data, genericClass));
    }

}

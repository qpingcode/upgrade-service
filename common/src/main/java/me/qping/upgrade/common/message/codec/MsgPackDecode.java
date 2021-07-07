package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.qping.upgrade.common.message.Msg;

import java.util.List;

public class MsgPackDecode extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
//        try{
//            final int length = msg.readableBytes();
//            final byte[] array = new byte[length];
//            msg.getBytes(msg.readerIndex(), array, 0, length);
//
//            MessagePack pack = new MessagePack();
//            pack.register(Object.class, ObjectTemplate.getInstance());
//
//            out.add(pack.read(array, Msg.class));
//        }catch (exception ex){
//            ex.printStackTrace();
//            throw ex;
//        }

        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        out.add(MessagePackUtil.toObject(bytes, Msg.class));

    }
}
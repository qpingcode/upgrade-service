package me.qping.upgrade.common.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgPackEncode extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {

//        try{
//
//            MessagePack pack = new MessagePack();
//            pack.register(Object.class, ObjectTemplate.getInstance());
//            byte[] write = pack.write(msg);
//            out.writeBytes(write);
//
//        }catch (exception ex){
//            ex.printStackTrace();
//            throw ex;
//        }

        byte[] raw =  MessagePackUtil.toBytes(msg);
        out.writeBytes(raw);

    }
}
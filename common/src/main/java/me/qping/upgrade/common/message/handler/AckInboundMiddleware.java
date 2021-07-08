package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.MsgStorage;

import static me.qping.upgrade.common.constant.MsgType.*;

public class AckInboundMiddleware extends ChannelInboundHandlerAdapter {

    protected String name;

    public AckInboundMiddleware(String name) {
        this.name = name;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Msg m = (Msg) msg;
        switch (m.getType()) {
            case REQUEST:
                handlerRequest(ctx, m);
                break;
            case RESPONSE:
                handlerResponse(ctx, m);
                break;
        }
    }


    public void handlerRequest(ChannelHandlerContext ctx, Msg msg) {

        System.out.println("server 接收数据 ： " +  msg.toString());

//        ctx.channel().writeAndFlush(msg);

//        System.out.println("server 发送数据： " + msg);

    }
    protected void handlerResponse(ChannelHandlerContext ctx, Msg msg) {
        MsgStorage.recive(msg);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println( name +"  exception" + cause.toString());
        ctx.close();
    }

}
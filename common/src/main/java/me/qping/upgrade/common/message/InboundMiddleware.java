package me.qping.upgrade.common.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import me.qping.upgrade.common.message.codec.MessagePackUtil;

import static me.qping.upgrade.common.constant.MsgType.*;

public abstract class InboundMiddleware extends ChannelInboundHandlerAdapter {

    protected String name;

    //记录次数
    private int heartbeatCount = 0;

    //获取server and client 传入的值
    public InboundMiddleware(String name) {
        this.name = name;
    }

    /**
     * 继承ChannelInboundHandlerAdapter实现了channelRead就会监听到通道里面的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Msg m = (Msg) msg;
        switch (m.getType()) {
            case REGISTER:
                handlerRegister(ctx, m);
            case PING:
                sendPong(ctx);
                break;
            case PONG:
                System.out.println(name + " get  pong  msg  from" + ctx.channel().remoteAddress());
                break;
            case REQUEST:
                handlerRequest(ctx, m);
                break;
            case RESPONSE:
                handlerResponse(ctx, m);
                break;
            default:
                break;
        }
    }

    protected void sendPing(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(Msg.ping());
        heartbeatCount++;
        System.out.println(name + " send ping msg to " + ctx.channel().remoteAddress() + " , count :" + heartbeatCount);
    }

    protected void sendPong(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(Msg.pong());
        heartbeatCount++;
    }

    public <T> T getResponse(Msg msg, Class<T> clazz){
       return MessagePackUtil.toObject(msg.getBody(), clazz);
    }



    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        IdleStateEvent stateEvent = (IdleStateEvent) evt;
        switch (stateEvent.state()) {
            case READER_IDLE:
                handlerReaderIdle(ctx);
                break;
            case WRITER_IDLE:
                handlerWriterIdle(ctx);
                break;
            case ALL_IDLE:
                handlerAllIdle(ctx);
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println( name +"  exception" + cause.toString());
        ctx.close();
    }

    protected void handlerResponse(ChannelHandlerContext ctx, Msg msg) {
        MsgFuture.recive(msg);
    }

    protected abstract void handlerRequest(ChannelHandlerContext ctx, Msg msg);

    protected void handlerRegister(ChannelHandlerContext ctx, Msg msg){
        System.out.println(((Msg) msg).getClientId() + " register into Server");
    }
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        System.err.println("---ALL_IDLE---");
    }
    protected void handlerWriterIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }
    protected void handlerReaderIdle(ChannelHandlerContext ctx) { System.err.println("---READER_IDLE---"); }

}
package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import me.qping.upgrade.common.message.impl.Ping;
import me.qping.upgrade.common.message.impl.Pong;
import me.qping.upgrade.common.message.impl.RegisterForm;
import me.qping.upgrade.common.message.impl.RegisterResponse;

public abstract class OnlineInboundMiddleware extends ChannelInboundHandlerAdapter {

    protected String name;

    //记录次数
    private int heartbeatCount = 0;

    //获取server and client 传入的值
    public OnlineInboundMiddleware(String name) {
        this.name = name;
    }

    /**
     * 继承ChannelInboundHandlerAdapter实现了channelRead就会监听到通道里面的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof RegisterForm){
            RegisterForm m = (RegisterForm) msg;
            handlerRegister(ctx, m);
            return;
        }

        if(msg instanceof RegisterResponse){
            RegisterResponse m = (RegisterResponse) msg;
            handlerRegisterResponse(ctx, m);
            return;
        }

        if(msg instanceof Ping){
            sendPong(ctx);
            return;
        }

        if(msg instanceof Pong){
            return;
        }

        super.channelRead(ctx, msg);

    }


    protected void sendPing(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(new Ping());
        heartbeatCount++;
    }

    protected void sendPong(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(new Pong());
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
        System.err.println(name);
        cause.printStackTrace();
        ctx.close();
    }

    protected void handlerRegister(ChannelHandlerContext ctx, RegisterForm msg){
        System.out.println("register: " + msg.getNodeId());
    }

    protected void handlerRegisterResponse(ChannelHandlerContext ctx, RegisterResponse msg) {
        System.out.println("register response");
    }

    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        System.err.println("---ALL_IDLE---");
    }
    protected void handlerWriterIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }
    protected void handlerReaderIdle(ChannelHandlerContext ctx) { System.err.println("---READER_IDLE---"); }

}
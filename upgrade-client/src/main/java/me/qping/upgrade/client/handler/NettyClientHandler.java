package me.qping.upgrade.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import me.qping.upgrade.common.exception.FatalException;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.InboundMiddleware;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.MsgFuture;
import me.qping.upgrade.common.message.impl.RegisterForm;
import me.qping.upgrade.common.message.codec.MessagePackUtil;
import me.qping.upgrade.common.message.impl.Response;
import me.qping.upgrade.common.message.impl.ResponseBase;

import static me.qping.upgrade.common.constant.MsgType.*;
import static me.qping.upgrade.common.constant.ServerConstant.RequestTimeout;

/**
 * @ClassName NettyClient
 * @Description 客户端
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class NettyClientHandler extends InboundMiddleware {

    Client client;
    String name;

    public NettyClientHandler(String name, Client client) {
        super(name);
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 注册到服务端
        byte[] body = MessagePackUtil.toBytes(new RegisterForm(1, "我想注册下"));

        Msg msg = Msg.register();
        msg.setClientId(client.getClientId());
        msg.setMessageId(client.getMessageId());
        msg.setBody(body);

        MsgFuture future = new MsgFuture(msg);
        ctx.writeAndFlush(msg);


        new Thread(new Runnable() {
            @Override
            public void run() {
                Msg responseMsg = future.get(RequestTimeout);

                if(responseMsg == null){
                    // 请求超时
                    ctx.close();
                    return;
                }

                ResponseBase response = getResponse(responseMsg, ResponseBase.class);
                if(response.getCode() != Response.SUCCESS){
                    System.err.println("无法上线，错误信息：" + response.getMessage());
                    client.disconnect();
                }else{
                    System.out.println(response.getMessage());
                }
            }
        }).start();

    }

    @Override
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        sendPing(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.connect();
    }


    @Override
    protected void handlerRequest(ChannelHandlerContext ctx, Msg msg) {

    }
}

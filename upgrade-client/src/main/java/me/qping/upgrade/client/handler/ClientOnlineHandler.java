package me.qping.upgrade.client.handler;

import io.netty.channel.ChannelHandlerContext;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.handler.OnlineInboundMiddleware;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.impl.RegisterForm;
import me.qping.upgrade.common.message.impl.Response;
import me.qping.upgrade.common.message.impl.ResponseBase;

/**
 * @ClassName NettyClient
 * @Description 客户端
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class ClientOnlineHandler extends OnlineInboundMiddleware {

    Client client;

    public ClientOnlineHandler(String name, Client client) {
        super(name);
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 注册到服务端
        Msg msg = Msg.register();
        msg.setClientId(client.getClientId());
        msg.setMessageId(client.getMessageId());
        msg.setBody(new RegisterForm(1, "我想注册下"));
        ctx.writeAndFlush(msg);
    }

    @Override
    protected void handlerRegisterResponse(ChannelHandlerContext ctx, Msg msg) {

        // 需要注意连接成功和上线成功是两种状态
        // 可能存在一个客户端启动多次的情况，此处需要等服务器判断有没有同样clientId的客户端登陆过，服务器判定可以登陆后，返回客户端成功消
        // 息。 客户端标记状态为上线。
        ResponseBase response = (ResponseBase) msg.getBody();
        if(response.getCode() != Response.SUCCESS){
            System.err.println("无法上线，错误信息：" + response.getMessage());
            client.setOnline(false);
            client.disconnect();
        }else{
            client.setOnline(true);
            System.out.println(response.getMessage());
        }
    }

    @Override
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        sendPing(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.setOnline(false);
        client.connect();
    }

}

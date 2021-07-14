package me.qping.upgrade.client.handler;

import io.netty.channel.ChannelHandlerContext;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.handler.OnlineInboundMiddleware;
import me.qping.upgrade.common.message.impl.ForceOffline;
import me.qping.upgrade.common.message.impl.RegisterForm;
import me.qping.upgrade.common.message.impl.RegisterResponse;
import me.qping.upgrade.common.constant.ResponseCode;

import java.util.Date;

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
        RegisterForm msg = new RegisterForm();
        msg.setNodeId(client.getNodeId());
        msg.setMessageId(client.getMessageId());
        msg.setCreateDate(new Date());
        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.setOnline(false);

        // 延迟一秒重连，可能会出现：异常下线，客户端立马重连，服务器还没来得及删session，报错重复登陆的问题
        try{
            Thread.sleep(1000);
        } catch (Exception ex){

        } finally {
            client.connect();
        }
    }

    @Override
    protected void handlerRegisterResponse(ChannelHandlerContext ctx, RegisterResponse msg) {
        // 需要注意连接成功和上线成功是两种状态
        // 可能存在一个客户端启动多次的情况，此处需要等服务器判断有没有同样clientId的客户端登陆过，服务器判定可以登陆后，返回客户端成功消
        // 息。 客户端标记状态为上线。
        if(msg.getCode() != ResponseCode.SUCCESS){
            System.err.println("无法上线，错误信息：" + msg.getMessage());
            client.setOnline(false);
            client.disconnect();
        }else{
            client.setOnline(true);
            System.out.println(msg.getMessage());
        }
    }

    @Override
    protected void handlerAllIdle(ChannelHandlerContext ctx) {
        sendPing(ctx);
    }

    @Override
    protected void handlerForceOffline(ChannelHandlerContext ctx, ForceOffline m){
        System.err.println("服务器命令强制下线！");
        client.setOnline(false);
        client.disconnect();
    }


}

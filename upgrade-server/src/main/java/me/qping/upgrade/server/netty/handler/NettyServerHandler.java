package me.qping.upgrade.server.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import me.qping.upgrade.common.message.InboundMiddleware;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.impl.Response;
import me.qping.upgrade.common.message.impl.ResponseBase;
import me.qping.upgrade.server.bean.ClientInfo;
import me.qping.upgrade.server.netty.UpgradeServer;
import java.text.SimpleDateFormat;

import static me.qping.upgrade.server.netty.UpgradeServer.clientChannelMap;

/**
 * @ClassName NettyServerHandler
 * @Description 服务器handler
 * @Author qping
 * @Date 2021/6/28 17:04
 * @Version 1.0
 **/
public class NettyServerHandler extends InboundMiddleware {

    public NettyServerHandler(String name) {
        super(name);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        AttributeKey<ClientInfo> key = AttributeKey.valueOf("client");
        if (!channel.hasAttr(key)) {
            return;
        }
        ClientInfo clientInfo = channel.attr(key).get();
        System.err.println("客户端下线：" + clientInfo.getId());
        clientChannelMap.remove(clientInfo.getId());
    }


    @Override
    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        System.err.println(" ---- client "+ ctx.channel().remoteAddress().toString() + " reader timeOut, --- close it");
        ctx.close();
    }

    @Override
    public void handlerRequest(ChannelHandlerContext ctx, Msg msg) {

        System.out.println("server 接收数据 ： " +  msg.toString());

        ctx.channel().writeAndFlush(msg);

        System.out.println("server 发送数据： " + msg);

    }


    public void handlerRegister(ChannelHandlerContext ctx, Msg msg) {

        Channel channel = ctx.channel();

        // 获取客户端信息
        long clientId = msg.getClientId();

        // 重复上线
        ClientInfo old = UpgradeServer.getClient(clientId);
        if(old != null){
            System.err.println("客户端上线失败，重复上线：" + clientId);

            ResponseBase response = new ResponseBase();
            response.setCode(Response.ERR_REG_REPEAT);
            response.setMessage("该ID已经上线，上线时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(old.getCreateDate()));

            ctx.writeAndFlush(Msg.response(msg.getMessageId(), response));
            return;
        }

        // 将客户端基本信息保存到 channel 的 attr 中
        ClientInfo c = new ClientInfo();
        c.setId(clientId);
        c.setAddress(channel.remoteAddress().toString());

        UpgradeServer.attachClient(channel, c);

        System.err.println("客户端上线: " + msg.getClientId());

        // 通知客户端操作成功
        ResponseBase response = new ResponseBase();
        response.setCode(Response.SUCCESS);
        response.setMessage("上线成功，时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getCreateDate()));
        ctx.writeAndFlush(Msg.response(msg.getMessageId(), response));

    }


}

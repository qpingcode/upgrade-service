package me.qping.upgrade.server.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import me.qping.upgrade.common.exception.ServerRegException;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.handler.OnlineInboundMiddleware;
import me.qping.upgrade.common.message.impl.Response;
import me.qping.upgrade.common.message.impl.ResponseBase;
import me.qping.upgrade.server.bean.ClientInfo;

import java.text.SimpleDateFormat;

import static me.qping.upgrade.server.netty.UpgradeServer.clientChannelMap;

/**
 * @ClassName NettyServerHandler
 * @Description 服务器handler
 * @Author qping
 * @Date 2021/6/28 17:04
 * @Version 1.0
 **/
public class ServerOnlineHandler extends OnlineInboundMiddleware {

    public ServerOnlineHandler(String name) {
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


    public void handlerRegister(ChannelHandlerContext ctx, Msg msg) {


        try{

            Channel channel = ctx.channel();

            // 获取客户端信息
            long clientId = msg.getClientId();

            if(clientId <= 0){
                throw new ServerRegException(Response.ERR_CLIENT_ID_ILLEGAL, "客户端上线失败，id非法：" + clientId);
            }

            // 重复上线
            ClientInfo old = UpgradeServer.getClient(clientId);
            if(old != null){
                throw new ServerRegException(Response.ERR_REG_REPEAT,"客户端上线失败，重复上线，上线时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(old.getCreateDate()));
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
            ctx.writeAndFlush(Msg.registerResponse(msg.getMessageId(), response));

        }catch (ServerRegException ex){
            System.err.println(ex.getMessage());

            ResponseBase response = new ResponseBase();
            response.setCode(ex.getResponseCode());
            response.setMessage(ex.getMessage());
            ctx.writeAndFlush(Msg.registerResponse(msg.getMessageId(), response));
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }


}

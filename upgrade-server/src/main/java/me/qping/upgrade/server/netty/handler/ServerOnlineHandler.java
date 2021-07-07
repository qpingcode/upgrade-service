package me.qping.upgrade.server.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.handler.OnlineInboundMiddleware;
import me.qping.upgrade.common.message.impl.Response;
import me.qping.upgrade.common.message.impl.ResponseBase;
import me.qping.upgrade.common.session.Session;
import me.qping.upgrade.common.session.SessionUtil;

import java.text.SimpleDateFormat;


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
        SessionUtil.unBindSession(channel);

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
                throw new ServerException(Response.ERR_REG_CLIENT_ID_ILLEGAL, "客户端上线失败，id非法：" + clientId);
            }

            // 重复上线
            Session old = SessionUtil.getSession(clientId);
            if(old != null){
                throw new ServerException(Response.ERR_REG_REPEAT, "客户端上线失败，重复上线，上线时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(old.getCreateDate()));
            }

            // 将客户端基本信息保存到 channel 的 attr 中
            Session session = new Session();
            session.setClientId(clientId);
            session.setAddress(channel.remoteAddress().toString());
            SessionUtil.bindSession(session, channel);

            // 通知客户端操作成功
            ResponseBase response = new ResponseBase();
            response.setCode(Response.SUCCESS);
            response.setMessage("上线成功，时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(session.getCreateDate()));
            ctx.writeAndFlush(Msg.registerResponse(msg.getMessageId(), response));

        }catch (ServerException ex){
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

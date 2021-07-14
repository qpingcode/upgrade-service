package me.qping.upgrade.server.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.qping.upgrade.common.constant.ResponseCode;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.handler.OnlineInboundMiddleware;
import me.qping.upgrade.common.message.impl.RegisterForm;
import me.qping.upgrade.common.message.impl.RegisterResponse;
import me.qping.upgrade.common.session.Session;
import me.qping.upgrade.common.session.SessionUtil;

import java.text.SimpleDateFormat;

import static me.qping.upgrade.common.constant.ResponseCode.ERR_OTHER;


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
        System.out.println("removed");
        Channel channel = ctx.channel();
        SessionUtil.unBindSession(channel);
    }


    @Override
    protected void handlerReaderIdle(ChannelHandlerContext ctx) {
        System.out.println("closed");
        SessionUtil.unBindSession(ctx.channel()); // add by qping 2021-07-13
        ctx.close();
    }

    public void handlerRegister(ChannelHandlerContext ctx, RegisterForm msg) {

        RegisterResponse response = new RegisterResponse();
        response.setMessageId(msg.getMessageId());

        try{

            Channel channel = ctx.channel();

            // 获取客户端信息
            long nodeId = msg.getNodeId();

            if(nodeId <= 0){
                throw new ServerException(ResponseCode.ERR_CLIENT_ID_ILLEGAL, "客户端上线失败，id非法：" + nodeId);
            }

            // 重复上线
            Session old = SessionUtil.getSession(nodeId);
            if(old != null){
                throw new ServerException(ResponseCode.ERR_REG_REPEAT, "客户端上线失败，重复上线，上线时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(old.getCreateDate()));
            }

            // 将客户端基本信息保存到 channel 的 attr 中
            Session session = new Session();
            session.setNodeId(nodeId);
            session.setAddress(channel.remoteAddress().toString());
            SessionUtil.bindSession(session, channel);

            // 通知客户端操作成功
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage("客户端：" + nodeId + " 上线成功，时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(session.getCreateDate()));

        }catch (Exception ex){
            System.err.println(ex.getMessage());
            if(ex instanceof ServerException){
                response.setCode(((ServerException)ex).getResponseCode());
            }else{
                response.setCode(ERR_OTHER);
            }
            response.setMessage(ex.getMessage());
        }finally {
            ctx.writeAndFlush(response);
        }

    }


}

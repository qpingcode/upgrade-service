package me.qping.upgrade.client.handler;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RuntimeUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.qping.upgrade.common.constant.MsgType;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.impl.Response;
import me.qping.upgrade.common.message.impl.ResponseBase;

import static me.qping.upgrade.common.constant.MsgType.SHELL_COMMAND;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class ShellCommandHandler extends ChannelInboundHandlerAdapter {

    Client client;

    public ShellCommandHandler(String name, Client client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        Msg msg = (Msg) message;
        if(msg.getType() == SHELL_COMMAND){

            new Thread(new Runnable() {
                @Override
                public void run() {

                    System.out.println("开始执行: " + msg.toString());

                    Msg reply = new Msg();
                    reply.setMessageId(msg.getMessageId());
                    reply.setClientId(client.getClientId());
                    reply.setType(MsgType.SHELL_COMMAND_RESPONSE);
                    try{
                        String result = RuntimeUtil.execForStr((String) msg.getBody());
                        reply.setBody(ResponseBase.of(Response.SUCCESS, result));
                    }catch (IORuntimeException e){
                        reply.setBody(ResponseBase.of(Response.ERR_COMMAND_ERROR, e.getMessage()));
                    }
                    ctx.writeAndFlush(reply);

                }
            }).start();

        }else{
            super.channelRead(ctx, message);
        }
    }
}

package me.qping.upgrade.server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.Msg;

import static me.qping.upgrade.common.constant.MsgType.SHELL_COMMAND_RESPONSE;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class ShellCommandHandler extends ChannelInboundHandlerAdapter {


    public ShellCommandHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        Msg msg = (Msg) message;
        if(msg.getType() == SHELL_COMMAND_RESPONSE){
            System.out.println("执行结果: " + msg.toString());
        }else{
            super.channelRead(ctx, message);
        }
    }
}

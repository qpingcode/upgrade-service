package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.message.MsgStorage;
import me.qping.upgrade.common.message.impl.ShellCommandResponse;

/**
 * @ClassName ShellCommandResponseHandler
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class ShellCommandResponseHandler extends SimpleChannelInboundHandler<ShellCommandResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ShellCommandResponse msg) throws Exception {
        MsgStorage.recive(msg);
        System.out.println("执行结果: " + msg.toString());
    }
}

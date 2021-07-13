package me.qping.upgrade.common.message.handler;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RuntimeUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.constant.ResponseCode;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.impl.ShellCommand;
import me.qping.upgrade.common.message.impl.ShellCommandResponse;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class ShellCommandHandler extends SimpleChannelInboundHandler<ShellCommand> {


    Client client;

    public ShellCommandHandler(Client client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ShellCommand msg) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {

                System.out.println("开始执行: " + msg.toString());

                ShellCommandResponse reply = new ShellCommandResponse();
                reply.setMessageId(msg.getMessageId());
                try{
                    String result = RuntimeUtil.execForStr((String) msg.getCommand());
                    reply.setMessage(result);
                    reply.setCode(ResponseCode.SUCCESS);
                }catch (IORuntimeException e){
                    reply.setMessage(e.getMessage());
                    reply.setCode(ResponseCode.ERR_COMMAND_ERROR);
                }
                System.out.println("结束执行: " + msg.toString());
                ctx.writeAndFlush(reply);

            }
        }).start();
    }
}

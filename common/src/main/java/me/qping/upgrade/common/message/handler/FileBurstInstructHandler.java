package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.impl.FileBurstData;
import me.qping.upgrade.common.message.impl.FileBurstInstruct;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileBurstInstructHandler extends SimpleChannelInboundHandler<FileBurstInstruct> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileBurstInstruct fileBurstInstruct) throws Exception {
        if (FileStatus.COMPLETE == fileBurstInstruct.getStatus()) {
            return;
        }
        FileBurstData fileBurstData = FileTransferUtil.readFile(fileBurstInstruct.getClientFileUrl(),
                fileBurstInstruct.getReadPosition());

        fileBurstData.setMessageId(fileBurstInstruct.getMessageId());
        ctx.writeAndFlush(fileBurstData);
    }

}

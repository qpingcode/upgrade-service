package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.message.MsgStorage;
import me.qping.upgrade.common.message.impl.FileAskResponse;

import java.io.File;

/**
 * @ClassName NettyClient
 * @Description 命令客户端上传文件
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileAskResponseHandler extends SimpleChannelInboundHandler<FileAskResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileAskResponse fileAskResponse){
        MsgStorage.recive(fileAskResponse);
        System.out.println("文件查看结果: " + fileAskResponse.toString());
    }

}

package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.message.impl.FileAsk;
import me.qping.upgrade.common.message.impl.FileAskResponse;

import java.io.File;

/**
 * @ClassName NettyClient
 * @Description 命令客户端上传文件
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileAskHandler extends SimpleChannelInboundHandler<FileAsk> {

    String basePath;

    public FileAskHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileAsk fileAsk){
        System.out.println("接收到服务器端询问文件：" + fileAsk.getFilePath());
        File file = new File(fileAsk.getFilePath());
        FileAskResponse fileAskResponse = new FileAskResponse(fileAsk.getMessageId());
        if(!file.exists()){
            fileAskResponse.setExists(false);
        }else{
            fileAskResponse.setExists(true);

            if(file.isDirectory()){
                fileAskResponse.setDir(true);
                if (null != file.listFiles()) {
                    for (File subFile : file.listFiles()) {
                        fileAskResponse.addFile(subFile.getAbsolutePath(), subFile.length(), subFile.getName());
                    }
                }
            }else{
                fileAskResponse.setDir(false);
                fileAskResponse.addFile(file.getAbsolutePath(), file.length(), file.getName());
            }
        }
        ctx.writeAndFlush(fileAskResponse);
    }

}

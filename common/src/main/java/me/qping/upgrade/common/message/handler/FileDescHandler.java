package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.impl.FileDesc;
import me.qping.upgrade.common.message.impl.FileProgress;
import me.qping.upgrade.common.message.progress.ProgressStorage;

import java.sql.SQLException;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileDescHandler extends SimpleChannelInboundHandler<FileDesc> {

    String basePath;

    ProgressStorage storage = ProgressStorage.getInstance();

    public FileDescHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileDesc fileDesc){

        System.out.println("开始接收文件名称：" + fileDesc.getFileName());

        if(fileDesc.isBreakPointResume()){
            // 文件接收方查询传输进度，然后发请求给文件提供方，请求数据
            FileProgress progress = null;
            try {
                progress = storage.findByNodeIdAndFilePathAndFileName(fileDesc.getNodeId(), fileDesc.getSourcePath(), fileDesc.getFileName());
                // 只有文件大小一致才可以断点续传，如果发生改变，无法断点续传
                if (null != progress && fileDesc.getFileSize() == progress.getTotalSize()) {
                    progress.clearDataAndPrepareToRead();
                    progress.setMessageId(fileDesc.getMessageId());
                    progress.setChunkSize(fileDesc.getChunkSize());
                    ctx.writeAndFlush(progress);
                    return;
                }
            } catch (Exception e) {
                System.err.println("断点查询失败：" + e.getMessage() + "， 文件名：" + fileDesc.getFileName());
                // 无法查询断点，只能重传
            }
        }

        FileProgress progress = FileProgress.of(fileDesc);
        try {
            storage.insert(progress);
        } catch (SQLException e) {
            progress.setStatus(FileStatus.ERROR);
            progress.setErrMsg("获取文件信息失败：" + e.getMessage());
        }
        ctx.writeAndFlush(progress);

    }

}

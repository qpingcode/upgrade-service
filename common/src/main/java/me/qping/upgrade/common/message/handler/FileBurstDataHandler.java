package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.impl.FileBurstData;
import me.qping.upgrade.common.message.impl.FileBurstInstruct;

import java.io.File;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileBurstDataHandler extends SimpleChannelInboundHandler<FileBurstData> {

    String basePath;

    public FileBurstDataHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileBurstData fileBurstData) throws Exception {

        long messageId = fileBurstData.getMessageId();

        String filePath = basePath + File.separator + "temp" + File.separator;
        FileTransferUtil.createDir(filePath);
        FileBurstInstruct fileBurstInstruct = FileTransferUtil.writeFile(filePath, fileBurstData);
        // 保存断点续传信息 todo
        ctx.writeAndFlush(fileBurstInstruct);

        // 传输完成删除断点信息
        if (fileBurstInstruct.getStatus() == FileStatus.COMPLETE) {
            System.out.println("上传完成，文件名：" + filePath + fileBurstData.getFileName());

            //todo 下载完移动到目标目录
            FileTransferUtil.remove(messageId);
            // 把日志文件从临时目录中移走
//            Files.move(uploadFilePath, saveUploadFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

    }


}

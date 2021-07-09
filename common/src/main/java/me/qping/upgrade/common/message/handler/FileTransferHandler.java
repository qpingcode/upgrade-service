package me.qping.upgrade.common.message.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RuntimeUtil;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import me.qping.upgrade.common.constant.MsgType;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.MsgStorage;
import me.qping.upgrade.common.message.impl.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static me.qping.upgrade.common.constant.MsgType.*;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileTransferHandler extends ChannelInboundHandlerAdapter {

    String basePath;

    public FileTransferHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        Msg msg = (Msg) message;
        switch (msg.getType()){
            case FILE_DESC_INFO:
                handlerDescInfo(ctx, msg);
                break;
            case FILE_BURST_INSTRUCT:
                handlerBurstInstruct(ctx, msg);
                break;
            case FILE_BURST_DATA:
                handlerBurstData(ctx, msg);
                break;
            default:
                super.channelRead(ctx, message);
        }
    }

    private void handlerDescInfo(ChannelHandlerContext ctx, Msg msg) {
        FileDescInfo fileDescInfo = (FileDescInfo) msg.getBody();

        String filePath = basePath + File.separator + msg.getClientId() + File.separator;
        System.out.println("开始上传，文件名称：" + fileDescInfo.getFileName() + "；存储路径：" + filePath);

        // todo 断点续传信息，实际应用中需要将断点续传信息保存到数据库中
        FileBurstInstruct fileBurstInstructOld = FileTransferUtil.get(msg.getMessageId());
        if (null != fileBurstInstructOld) {
            if (fileBurstInstructOld.getStatus() == FileStatus.COMPLETE) {
                FileTransferUtil.remove(msg.getMessageId());
            }
            System.out.println("传输完成删除断点信息" + JSON.toJSONString(fileBurstInstructOld));
            ctx.writeAndFlush(FileTransferUtil.buildTransferInstruct(msg.getMessageId(), fileBurstInstructOld));
            return;
        }

        Msg sendFileTransferProtocol = FileTransferUtil.buildTransferInstruct(msg.getMessageId(), FileStatus.BEGIN, fileDescInfo.getFileUrl(), 0l);
        ctx.writeAndFlush(sendFileTransferProtocol);
    }

    private void handlerBurstData(ChannelHandlerContext ctx, Msg msg) throws IOException {
        FileBurstData fileBurstData = (FileBurstData) msg.getBody();

        String filePath = basePath + File.separator + msg.getClientId() + File.separator;
        FileTransferUtil.createDir(filePath);
        FileBurstInstruct fileBurstInstruct = FileTransferUtil.writeFile(filePath, fileBurstData);

        // 保存断点续传信息
        FileTransferUtil.put(msg.getMessageId(), fileBurstInstruct);
        ctx.writeAndFlush(FileTransferUtil.buildTransferInstruct(msg.getMessageId(), fileBurstInstruct));

        // 传输完成删除断点信息
        if (fileBurstInstruct.getStatus() == FileStatus.COMPLETE) {
            System.out.println("上传完成，文件名：" + filePath + fileBurstData.getFileName());

            FileTransferUtil.remove(msg.getMessageId());
            // 把日志文件从临时目录中移走
//            Files.move(uploadFilePath, saveUploadFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void handlerBurstInstruct(ChannelHandlerContext ctx, Msg msg) throws IOException {
        FileBurstInstruct fileBurstInstruct = (FileBurstInstruct) msg.getBody();
        if (FileStatus.COMPLETE == fileBurstInstruct.getStatus()) {
            return;
        }
        FileBurstData fileBurstData = FileTransferUtil.readFile(fileBurstInstruct.getClientFileUrl(),
                fileBurstInstruct.getReadPosition());

        ctx.writeAndFlush(FileTransferUtil.buildTransferData(msg.getMessageId(), fileBurstData));
    }


}

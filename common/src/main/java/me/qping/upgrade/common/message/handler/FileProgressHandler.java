package me.qping.upgrade.common.message.handler;

import io.netty.channel.*;
import me.qping.upgrade.common.constant.FileOperFlag;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.impl.FileData;
import me.qping.upgrade.common.message.impl.FileProgress;
import me.qping.upgrade.common.message.impl.FileProgressListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileProgressHandler extends SimpleChannelInboundHandler<FileProgress> {

    String basePath;
    static List<FileProgressListener> listeners = new CopyOnWriteArrayList<>();

    public FileProgressHandler(String basePath) {
        this.basePath = basePath;
    }

    public static void addListener(FileProgressListener listener){
        FileProgressHandler.listeners.add(listener);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileProgress fileProgress) {

        if(fileProgress.getFlag() == FileOperFlag.READ){
            readData(ctx, fileProgress);
        }else if(fileProgress.getFlag() == FileOperFlag.WRITE){
            writeDate(ctx, fileProgress);
        }
    }

    public Path getUploadTempPath(String targetPath){
        return Paths.get(basePath, targetPath, "temp");
    }

    public Path getUploadPath(String targetPath){
        return Paths.get(basePath, targetPath);
    }


    /**
     * 接收数据，并返回进度
     * 每次处理 FileProgress.readData 后
     *
     * @param ctx
     * @param progress
     */
    private void writeDate(ChannelHandlerContext ctx, FileProgress progress) {

        if(progress.getStatus() == FileStatus.ERROR){
            listeners.forEach(listener -> {
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourceUrl(), progress.getErrMsg());
            });
            return;
        }

        Path uploadTempPath = getUploadTempPath(progress.getTargetUrl());
        try {

            long readPosition = progress.getReadPosition();
            int readBytes = progress.getBytes() == null ? 0 : progress.getBytes().length;

            // 第一次写数据时，创建目录
            if(readPosition == 0){
                FileTransferUtil.createDir(uploadTempPath);
            }

            // 写入数据
            if(readBytes > 0){
                FileTransferUtil.writeFile(uploadTempPath, readPosition, progress.getBytes());
                listeners.forEach(listener -> {
                    listener.progress(progress.getId(), progress.getNodeId(), progress.getSourceUrl(), progress.getTotalSize(), progress.getReadPosition() + readBytes);
                });

            }

            // 没有更多数据啦
            if(progress.getStatus() == FileStatus.END){
                Path uploadPath = getUploadPath(progress.getTargetUrl());
                Files.move(uploadTempPath, uploadPath, StandardCopyOption.REPLACE_EXISTING);

                listeners.forEach(listener -> {
                    listener.end(progress.getId(), progress.getNodeId(), progress.getSourceUrl());
                });


                // 回写一个end给read
                // 为什么end和error都要回写给read？
                // 因为当服务器端下发文件时，写文件发生在客户端，读文件发生在服务器，当写完了不告诉服务器的话，服务器无法知道整个流程结束了。

            }

            // 请求下一块数据
            progress.setReadPosition(readPosition + readBytes);
            progress.clearDataAndPrepareToRead();

        } catch (Exception e) {

            listeners.forEach(listener -> {
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourceUrl(), "写入错误：" + e.getMessage());
            });



            // 回写一个error给read
            progress.clearDataAndPrepareToRead();
            progress.setStatus(FileStatus.ERROR);
            progress.setErrMsg(e.getMessage());
        }

        ctx.writeAndFlush(progress);
    }

    /**
     * 读取数据上传
     * readData 发起，可能的调用方
     *          1、服务器下发文件给客户端，首先发送 FileDesc 给客户端， 客户端收到后发送 FileProgress, 服务器接收后readData
     *          2、服务器要求客户端上传文件时，发了一个 FileProgress 给客户端, 客户端接收后readData
     *          3、每次处理 FileProgress.writeDate 后 readData
     *
     * @param ctx
     * @param progress
     */
    private void readData(ChannelHandlerContext ctx, FileProgress progress) {

        if(FileStatus.ERROR == progress.getStatus()){
            // 这里的错误可能来自 FileDescHandler 或者 FileProgressHandler.writeDate 方法
            listeners.forEach(listener -> {
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourceUrl(), progress.getErrMsg());
            });
            return;
        }

        if (FileStatus.END == progress.getStatus()) {
            listeners.forEach(listener -> {
                listener.end(progress.getId(), progress.getNodeId(), progress.getSourceUrl());
            });
            return;
        }

        // 读的一端能记录进度
        listeners.forEach(listener -> {
            listener.progress(progress.getId(), progress.getNodeId(), progress.getSourceUrl(), progress.getTotalSize(), progress.getReadPosition());
        });

        FileData fileData = FileTransferUtil.readFile(
                progress.getTotalSize(),
                progress.getSourceUrl(),
                progress.getReadPosition(),
                progress.getChunkSize()
        );
        int readBytes = fileData.getBytes().length;

        progress.setFlag(FileOperFlag.WRITE);
        progress.setBytes(fileData.getBytes());

        if(fileData.getErr() != null){
            progress.setStatus(FileStatus.ERROR);
            progress.setErrMsg("读取错误：" + fileData.getErr());

            listeners.forEach(listener -> {
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourceUrl(), progress.getErrMsg());
            });

        } else if(readBytes < progress.getChunkSize()){
            progress.setStatus(FileStatus.END);


        } else if(readBytes == progress.getChunkSize()){
            progress.setStatus(FileStatus.CENTER);
        }

        ctx.writeAndFlush(progress);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
    }

}

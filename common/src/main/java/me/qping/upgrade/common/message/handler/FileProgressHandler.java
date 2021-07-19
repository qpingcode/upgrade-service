package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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

import static me.qping.upgrade.common.constant.ServerConstant.SERVER_NODE_ID;
import static me.qping.upgrade.common.session.SessionUtil.transferStopList;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileProgressHandler extends SimpleChannelInboundHandler<FileProgress> {

    String basePath;
    String tempPath;
    static long currentNodeId;

    static List<FileProgressListener> listeners = new CopyOnWriteArrayList<>();

    public FileProgressHandler(String basePath, String tempPath, long nodeId) {
        this.basePath = basePath;
        this.tempPath = tempPath;
        this.currentNodeId = nodeId;
    }

    public static void addListener(FileProgressListener listener){
        FileProgressHandler.listeners.add(listener);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileProgress progress) {

        if(transferStopList.contains(progress.getId())){
            System.out.println("传输任务，id： " + progress.getId() + " 已被手动终止。");
            return;
        }

        if(progress.getFlag() == FileOperFlag.READ){
            progress = readData(progress);
        }else if(progress.getFlag() == FileOperFlag.WRITE){
            progress = writeDate(progress);
        }

        if(progress != null){
            ctx.writeAndFlush(progress);
        }

    }

    public Path getUploadTempPath(){
        return Paths.get(tempPath);
    }

    public Path getUploadPath(String targetPath){
        return Paths.get(basePath).resolve(targetPath);
    }


    /**
     * 接收数据，并返回进度
     * 每次处理 FileProgress.readData 后
     * @param progress
     */
    public FileProgress writeDate(FileProgress progress) {
        if(progress.getStatus() == FileStatus.ERROR){
            listeners.forEach(listener -> {
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourcePath(), progress.getErrMsg());
            });
            return null;
        }

        try {

            String fileName = progress.getFileName();
            Path uploadTempPath = getUploadTempPath();
            Path uploadTempFilePath = uploadTempPath.resolve(fileName);

            long readPosition = progress.getReadPosition();
            int readBytes = progress.getBytes() == null ? 0 : progress.getBytes().length;

            // 第一次写数据时，创建目录
            if(readPosition == 0){
                FileTransferUtil.createDir(uploadTempPath);
            }

            // 写入数据
            if(readBytes > 0){
                FileTransferUtil.writeFile(uploadTempFilePath, readPosition, progress.getBytes());
                listeners.forEach(listener -> {
                    listener.progress(progress.getId(), progress.getNodeId(), progress.getSourcePath(), progress.getTotalSize(), progress.getReadPosition() + readBytes);
                });

            }

            // 没有更多数据啦
            if(progress.getStatus() == FileStatus.END){
                Path uploadPath = getUploadPath(progress.getTargetPath());

                if(!uploadPath.getParent().toFile().exists()){
                    uploadPath.getParent().toFile().mkdirs();
                }

                Files.move(uploadTempFilePath, uploadPath.getParent().resolve(uploadPath.getFileName()), StandardCopyOption.ATOMIC_MOVE);

                listeners.forEach(listener -> {
                    listener.end(progress.getId(), progress.getNodeId(), progress.getSourcePath(), progress.getReadPosition() + readBytes);
                });



            }

            // 请求下一块数据
            progress.setReadPosition(readPosition + readBytes);
            progress.clearDataAndPrepareToRead();
        } catch (Exception e) {
            listeners.forEach(listener -> {
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourcePath(), "写入错误：" + e.getMessage());
            });



            // 回写一个error给read
            progress.clearDataAndPrepareToRead();
            progress.setStatus(FileStatus.ERROR);
            progress.setErrMsg(e.getMessage());
        }

        // 原则上写处理器发现已经end了，就停止所有流程
        // 客户端 write 时发现 end，需要 回写一个end给read
        if(currentNodeId == SERVER_NODE_ID && (progress.getStatus() == FileStatus.ERROR || progress.getStatus() == FileStatus.END)){
            // 服务器
            return null;
        }

        return progress;
    }

    /**
     * 读取数据上传
     * readData 发起，可能的调用方
     *          1、服务器要求客户端上传文件时，发了一个 FileProgress 给客户端, 客户端接收后readData
     *          2、每次处理 FileProgress.writeDate 后 readData
     * @param progress
     */
    public static FileProgress readData(FileProgress progress) {

        if(FileStatus.ERROR == progress.getStatus()){
            // 这里的错误来自 FileProgressHandler.writeDate 方法
            listeners.forEach(listener -> {
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourcePath(), progress.getErrMsg());
            });
            return null;
        }

        if (FileStatus.END == progress.getStatus()) {
            listeners.forEach(listener -> {
                listener.end(progress.getId(), progress.getNodeId(), progress.getSourcePath(), progress.getReadPosition());
            });
            return null;
        }

        // 读的一端能记录进度
        listeners.forEach(listener -> {
            listener.progress(progress.getId(), progress.getNodeId(), progress.getSourcePath(), progress.getTotalSize(), progress.getReadPosition());
        });

        FileData fileData = FileTransferUtil.readFile(
                progress.getTotalSize(),
                progress.getSourcePath(),
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
                listener.error(progress.getId(), progress.getNodeId(), progress.getSourcePath(), progress.getErrMsg());
            });

        } else if(readBytes < progress.getChunkSize()){
            progress.setStatus(FileStatus.END);
        } else if(readBytes == progress.getChunkSize()){
            progress.setStatus(FileStatus.CENTER);
        }
        return progress;
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(cause.getMessage());
    }

    public static void main(String[] args) throws IOException {

        String uploadPathStr = "/Users/qping/Desktop/data/server2/jrebel2/";
        Path uploadPath = Paths.get(uploadPathStr);


        System.out.println(uploadPath.getFileName());
        System.out.println(uploadPath.getParent());
        System.out.println(uploadPath.getParent().resolve(uploadPath.getFileName()));


        Files.move(Paths.get("/Users/qping/Desktop/data/server/temp/1.txt"), Paths.get("/Users/qping/Desktop/data/server/to/jrebel.log"), StandardCopyOption.REPLACE_EXISTING);

    }

}

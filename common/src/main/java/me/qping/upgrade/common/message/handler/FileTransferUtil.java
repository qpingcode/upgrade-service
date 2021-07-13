package me.qping.upgrade.common.message.handler;

import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.impl.FileBurstData;
import me.qping.upgrade.common.message.impl.FileBurstInstruct;
import me.qping.upgrade.common.message.impl.FileDescInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName FileUtil
 * @Description
 * @Author qping
 * @Date 2021/7/8 17:01
 * @Version 1.0
 **/
public class FileTransferUtil {

    public static Map<Long, FileBurstInstruct> burstDataMap = new ConcurrentHashMap<>();

    public static FileBurstInstruct get(Long fileKey){
        return burstDataMap.get(fileKey);
    }

    public static void put(Long fileKey, FileBurstInstruct fileBurstInstruct){
        burstDataMap.put(fileKey, fileBurstInstruct);
    }

    public static void remove(Long fileKey){
        burstDataMap.remove(fileKey);
    }

    public static FileDescInfo buildRequestTransferFile(long messageId, String fileUrl, String fileName, String fileType, Long fileSize) {
        FileDescInfo fileDescInfo = new FileDescInfo();
        fileDescInfo.setMessageId(messageId);
        fileDescInfo.setFileUrl(fileUrl);
        fileDescInfo.setFileName(fileName);
        fileDescInfo.setFileType(fileType);
        fileDescInfo.setFileSize(fileSize);
        return fileDescInfo;
    }

    /**
     * 构建对象；文件传输指令(服务端)
     * @param status          0请求传输文件、1文件传输指令、2文件传输数据
     * @param clientFileUrl   客户端文件地址
     * @param readPosition    读取位置
     * @return                传输协议
     */
    public static FileBurstInstruct buildTransferInstruct(long messageId, Integer status, String clientFileUrl, Long readPosition, Long totalSize) {

        FileBurstInstruct fileBurstInstruct = new FileBurstInstruct();
        fileBurstInstruct.setMessageId(messageId);
        fileBurstInstruct.setStatus(status);
        fileBurstInstruct.setClientFileUrl(clientFileUrl);
        fileBurstInstruct.setReadPosition(readPosition);
        fileBurstInstruct.setTotalSize(totalSize);

        return fileBurstInstruct;
    }


    public static int SEND_FILEDATA_LENGTH = 1024 * 100;    // 一次 100 K

    public static FileBurstData readFile(String fileUrl, Long readPosition) throws IOException {
        File file = new File(fileUrl);
        // r: 只读模式 rw:读写模式
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        randomAccessFile.seek(readPosition);
        byte[] bytes = new byte[SEND_FILEDATA_LENGTH];
        int readSize = randomAccessFile.read(bytes);
        if (readSize <= 0) {
            randomAccessFile.close();
            return new FileBurstData(FileStatus.COMPLETE);
        }
        FileBurstData fileInfo = new FileBurstData();
        fileInfo.setFileUrl(fileUrl);
        fileInfo.setFileName(file.getName());
        fileInfo.setBeginPos(readPosition);
        fileInfo.setEndPos(readPosition + readSize);
        // 不足1024需要拷贝去掉空字节
        if (readSize < SEND_FILEDATA_LENGTH) {
            byte[] copy = new byte[readSize];
            System.arraycopy(bytes, 0, copy, 0, readSize);
            fileInfo.setBytes(copy);
            fileInfo.setStatus(FileStatus.END);
        } else {
            fileInfo.setBytes(bytes);
            fileInfo.setStatus(FileStatus.CENTER);
        }
        randomAccessFile.close();
        return fileInfo;
    }

    public static FileBurstInstruct writeFile(String baseUrl, FileBurstData fileBurstData) throws IOException {

        if (FileStatus.COMPLETE == fileBurstData.getStatus()) {
            // FileStatus ｛0开始、1中间、2结尾、3完成｝
            return new FileBurstInstruct(FileStatus.COMPLETE);
        }

        File file = new File(baseUrl + "/" + fileBurstData.getFileName());
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.seek(fileBurstData.getBeginPos());
        randomAccessFile.write(fileBurstData.getBytes());
        randomAccessFile.close();

        if (FileStatus.END == fileBurstData.getStatus()) {
            return new FileBurstInstruct(FileStatus.COMPLETE);
        }


        FileBurstInstruct fileBurstInstruct = new FileBurstInstruct();
        fileBurstInstruct.setStatus(FileStatus.CENTER);
        fileBurstInstruct.setClientFileUrl(fileBurstData.getFileUrl());
        fileBurstInstruct.setReadPosition(fileBurstData.getEndPos());
        return fileBurstInstruct;
    }

    public static void createDir(String baseDir) {
        File file = new File(baseDir);
        if (!file.exists()) file.mkdirs();
    }


}

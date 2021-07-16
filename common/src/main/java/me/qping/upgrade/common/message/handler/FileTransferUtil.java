package me.qping.upgrade.common.message.handler;

import me.qping.upgrade.common.message.impl.FileData;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import static me.qping.upgrade.common.constant.ServerConstant.MIN_CHUCK_SIZE;

/**
 * @ClassName FileUtil
 * @Description
 * @Author qping
 * @Date 2021/7/8 17:01
 * @Version 1.0
 **/
public class FileTransferUtil {

    public static FileData readFile(long totalSize, String filePath, Long readPosition, int chunkSize){
        File file = new File(filePath);
        FileData fileData = new FileData();

        // 不能分的太小
        if(chunkSize < MIN_CHUCK_SIZE){
            chunkSize = MIN_CHUCK_SIZE;
        }

        // r: 只读模式 rw:读写模式
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {

            long rafLen = randomAccessFile.length();
            if(rafLen != totalSize){
                throw new Exception(String.format("源文件大小已经发生改变，原始 %s 当前 %s，停止传输文件", totalSize, rafLen));
            }


            randomAccessFile.seek(readPosition);
            byte[] bytes = new byte[chunkSize];
            int readSize = randomAccessFile.read(bytes);
            if (readSize <= 0) {
                randomAccessFile.close();
                fileData.setBytes(new byte[0]);
                return fileData;
            }

            if (readSize < chunkSize) {
                // 不足1024需要拷贝去掉空字节
                byte[] copy = new byte[readSize];
                System.arraycopy(bytes, 0, copy, 0, readSize);
                fileData.setBytes(copy);
            } else {
                fileData.setBytes(bytes);
            }

        } catch (Exception ex) {
            fileData.setErr(ex.getMessage());
        }
        return fileData;
    }

    public static void writeFile(Path filePath, long readPosition, byte[] data) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "rw");
        randomAccessFile.seek(readPosition);
        randomAccessFile.write(data);
        randomAccessFile.close();

    }

    public static void createDir(Path baseDir) {
        File file = baseDir.toFile();
        if (!file.exists()) file.mkdirs();
    }


}

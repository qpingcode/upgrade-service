package me.qping.upgrade.common.message.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.qping.upgrade.common.constant.FileOperFlag;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.Msg;

/**
 * 文件分片指令
 *
 * @author admin
 */
@Data
@NoArgsConstructor
public class FileProgress extends Msg {
    int id;               // 传输id
    long nodeId;          // 文件来源节点id
    byte flag;            // 1 读 2 写
    String sourceUrl;     // 文件URL
    String targetUrl;     // 目标路径

    long totalSize;       // 文件总大小
    long readPosition;    // 开始读取位置
    int chunkSize;        // 分块大小
    byte status;           // FileStatus 1 中间 2 结束 3 错误

    private String errMsg;     // 错误信息
    private byte[]	bytes;		// 文件字节；再实际应用中可以使用非对称加密，以保证传输信息安全

    public static FileProgress of(FileDesc fileDesc) {
        FileProgress progress = new FileProgress();
        progress.setMessageId(fileDesc.getMessageId());

        progress.setNodeId(fileDesc.getNodeId());

        progress.setSourceUrl(fileDesc.getSourceUrl());
        progress.setTargetUrl(fileDesc.getTargetUrl());

        progress.setTotalSize(fileDesc.getFileSize());
        progress.setChunkSize(fileDesc.getChunkSize());
        progress.setReadPosition(0);
        progress.setStatus(FileStatus.CENTER);

        progress.clearDataAndPrepareToRead();

        return progress;
    }


    public void clearDataAndPrepareToRead(){
        this.errMsg = null;
        this.bytes = null;
        this.flag = FileOperFlag.READ;  // 只有将进度传给发送方时，才需要清空data，这时候的模式是一个读取数据的请求
    }

}

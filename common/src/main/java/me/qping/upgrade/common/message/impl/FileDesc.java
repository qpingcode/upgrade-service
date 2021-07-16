package me.qping.upgrade.common.message.impl;

import lombok.Data;
import me.qping.upgrade.common.message.Msg;

/**
 * 上传文件请求
 * 包含文件的基本信息
 *
 * @author admin
 */
@Data
public class FileDesc extends Msg {

    private long nodeId;                 // 节点编号
    private String sourcePath;            // 文件路径
    private String targetPath;            // 目标存储路径
    private String fileName;             // 文件名称
    private String fileType;             // 文件类型（业务日志、存储日志、升级文件）
    private long fileSize;               // 文件大小
    private int chunkSize;              // 分块大小
    private boolean breakPointResume;    // 断点续传


    public static FileDesc of(long messageId, String sourcePath, String fileName, Long fileSize, String targetPath, long nodeId, int chunkSize) {
        FileDesc desc = new FileDesc();
        desc.setMessageId(messageId);
        desc.setSourcePath(sourcePath);
        desc.setFileName(fileName);
        desc.setFileSize(fileSize);
        desc.setTargetPath(targetPath);
        desc.setChunkSize(chunkSize);
        desc.setNodeId(nodeId);
        return desc;
    }

}

package me.qping.upgrade.common.message.impl;

/**
 * @ClassName FileProgressListener
 * @Description 文件传输进度监听
 * @Author qping
 * @Date 2021/7/15 16:41
 * @Version 1.0
 **/
public interface FileProgressListener {

    public void end(int progressId, long fileNodeId, String sourcePath);

    public void progress(int progressId, long fileNodeId, String sourcePath, long totalSize, long readPosition);

    public void error(int progressId, long fileNodeId, String sourcePath, String errorMsg);

}

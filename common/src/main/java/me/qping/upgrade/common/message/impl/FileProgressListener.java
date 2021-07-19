package me.qping.upgrade.common.message.impl;

/**
 * @ClassName FileProgressListener
 * @Description 文件传输进度监听
 * @Author qping
 * @Date 2021/7/15 16:41
 * @Version 1.0
 **/
public interface FileProgressListener {

    public void stop(FileProgress progress);

    public void end(FileProgress progress, long position);

    public void progress(FileProgress progress, long position);

    public void error(FileProgress progress, String errorMsg);

}

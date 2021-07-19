package me.qping.upgrade.common.message.impl;

public class FileBean {
    public String filePath;            // 文件路径
    public long fileSize;            // 文件路径
    public String fileName;

    public FileBean(String filePath, long fileSize, String fileName) {
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
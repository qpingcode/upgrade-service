package me.qping.upgrade.common.constant;

public interface FileStatus {
    byte CENTER = 1;    // 中间      回应
    byte END = 2;       // 结尾		 回应
    byte ERROR = 4;     // 错误
    byte STOP = 5;
}
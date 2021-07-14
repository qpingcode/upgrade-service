package me.qping.upgrade.common.message.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.qping.upgrade.common.message.Msg;

/**
 * 文件分片指令
 * @author admin
 *
 */
@Data
@NoArgsConstructor
public class FileBurstInstruct extends Msg {

    private int status;       // FileStatus ｛0开始、1中间、2结尾、3完成｝

    private String clientFileUrl; // 文件URL

    private long totalSize;       // 文件总大小

    private long readPosition;    // 读取位置

    public FileBurstInstruct(int status) {
        this.status = status;
    }
}

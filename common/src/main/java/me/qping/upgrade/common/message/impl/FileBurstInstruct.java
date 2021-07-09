package me.qping.upgrade.common.message.impl;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件分片指令
 * @author admin
 *
 */
@Data
@NoArgsConstructor
public class FileBurstInstruct {
    private Integer status;       //Constants.FileStatus ｛0开始、1中间、2结尾、3完成｝
    private String clientFileUrl; //客户端文件URL
    private Long readPosition; //读取位置

    public FileBurstInstruct(int status) {
        this.status = status;
    }
}

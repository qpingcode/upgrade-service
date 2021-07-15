package me.qping.upgrade.common.message.impl;

import lombok.Data;
import me.qping.upgrade.common.message.Msg;

/**
 * 文件描述信息
 *
 * @author admin
 */
@Data
public class FileAskResponse extends Msg {
    boolean exists;
    private String fileUrl;            // 文件路径
    private long fileSize;            // 文件路径
    private String fileName;
}

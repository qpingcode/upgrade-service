package me.qping.upgrade.common.message.impl;

import lombok.Data;
import me.qping.upgrade.common.message.Msg;

/**
 * 文件描述信息
 *
 * @author admin
 */
@Data
public class FileAsk extends Msg {
    private String fileUrl;            // 文件路径

    public FileAsk(String filePath) {
        this.fileUrl = filePath;
    }
}

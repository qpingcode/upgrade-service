package me.qping.upgrade.common.message.impl;

import lombok.Data;
import me.qping.upgrade.common.message.Msg;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件描述信息
 *
 * @author admin
 */
@Data
public class FileAskResponse extends Msg {
    boolean exists;
    boolean isDir;
    List<FileBean>  fileBeans = new ArrayList<FileBean>();

    public FileAskResponse(long messageId) {
        this.messageId = messageId;
    }

    public void addFile(String filePath, long fileSize, String fileName){
        this.fileBeans.add(new FileBean(filePath, fileSize, fileName));
    }
}

package me.qping.upgrade.common.message.impl;

import lombok.Data;
import me.qping.upgrade.common.message.Msg;

/**
 * @ClassName ShellCommand
 * @Description Shell命令返回
 * @Author qping
 * @Date 2021/7/13 14:50
 * @Version 1.0
 **/
@Data
public class ShellCommandResponse extends Msg {
    long code;
    String message;
}

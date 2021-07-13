package me.qping.upgrade.common.message.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.qping.upgrade.common.message.Msg;

/**
 * @ClassName ShellCommand
 * @Description shell命令请求
 * @Author qping
 * @Date 2021/7/13 14:50
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShellCommand extends Msg {
    String command;

    @Override
    public String toString() {
        return "ShellCommand{" +
                "messageId=" + this.getMessageId() + ", " +
                "command='" + command + '\'' +
                '}';
    }
}

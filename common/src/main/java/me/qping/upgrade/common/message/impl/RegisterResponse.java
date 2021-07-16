package me.qping.upgrade.common.message.impl;

import lombok.Data;
import me.qping.upgrade.common.message.Msg;

/**
 * @ClassName RegisterResponse
 * @Description 注册返回消息
 * @Author qping
 * @Date 2021/7/13 14:48
 * @Version 1.0
 **/
@Data
public class RegisterResponse extends Msg {
    long code;
    String message;

    public RegisterResponse(long messageId) {
        this.messageId = messageId;
    }
}

package me.qping.upgrade.common.message.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.qping.upgrade.common.message.Msg;

import java.util.Date;

/**
 * @ClassName RegisterMsg
 * @Description 注册请求消息
 * @Author qping
 * @Date 2021/7/6 11:50
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterForm extends Msg {
    long nodeId;
    Date createDate;
}

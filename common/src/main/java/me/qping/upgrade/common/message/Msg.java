package me.qping.upgrade.common.message;

import lombok.Data;
import me.qping.upgrade.common.constant.MsgType;

import java.io.Serializable;

@Data
public class Msg{
    long messageId; // 消息id
}
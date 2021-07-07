package me.qping.upgrade.common.constant;


import java.io.Serializable;

/**
 * @ClassName MsgType
 * @Description 消息类型
 * @Author qping
 * @Date 2021/7/2 09:32
 * @Version 1.0
 **/
public class  MsgType {
    public static final byte REGISTER = 0;
    public static final byte PING = 1;
    public static final byte PONG = 2;
    public static final byte REQUEST = 3;
    public static final byte RESPONSE = 4;
}

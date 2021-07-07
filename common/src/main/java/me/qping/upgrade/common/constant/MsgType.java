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
    public static final byte REGISTER = 0;              // 客户端向服务端发起注册请求
    public static final byte REGISTER_RESPONSE = 1;     // 服务端响应客户端的注册
    public static final byte PING = 2;          // 客户端空闲时向服务端 ping
    public static final byte PONG = 3;          // 服务端回应客户端ping 发出 pong
    public static final byte REQUEST = 4;       // 请求
    public static final byte RESPONSE = 5;      // 回应

    public static final byte SHELL_COMMAND = 9;
    public static final byte SHELL_COMMAND_RESPONSE = 10;

    public static final byte FILE_DESC_INFO = 16;            // 文件传输请求
    public static final byte FILE_BURST_INSTRUCT = 17;       // 文件传输指令
    public static final byte FILE_BURST_DATA = 18;           // 文件传输数据

}

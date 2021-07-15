package me.qping.upgrade.common.constant;

import me.qping.upgrade.common.message.impl.*;
import me.qping.upgrade.common.message.impl.FileProgress;

public enum MsgType {

    REGISTER((byte) 1, RegisterForm.class),                         // 客户端向服务端发起注册请求
    REGISTER_RESPONSE((byte) 2, RegisterResponse.class),            // 服务端响应客户端的注册
    PING((byte) 3, Ping.class),                                     // 客户端空闲时向服务端 ping
    PONG((byte) 4, Pong.class),                                     // 服务端回应客户端ping 发出 pong
    SHELL_COMMAND((byte) 5, ShellCommand.class),                    // 执行shell请求
    SHELL_COMMAND_RESPONSE((byte) 6, ShellCommandResponse.class),   // 执行shell返回
    FORCE_OFFLINE((byte) 7, ForceOffline.class),                    // 强制客户端下线命令


    FILE_DESC((byte) 16, FileDesc.class),                           // 文件上传请求
    FILE_PROGRESS((byte) 17, FileProgress.class),                   // 文件传输指令
    FILE_ASK((byte)18, FileAsk.class),
    FILE_ASK_RESPONSE((byte)19, FileAskResponse.class)
    ;

    byte val;
    Class<?> protocolStruct;

    private MsgType(byte val, Class<?> protocolStruct) {
        this.val = val;
        this.protocolStruct = protocolStruct;
    }

    public byte val(){
        return val;
    }

    public Class<?> protocolStruct(){
        return protocolStruct;
    }

}

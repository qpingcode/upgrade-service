package me.qping.upgrade.common.constant;

import me.qping.upgrade.common.message.impl.*;

public enum MsgTypeEnum {

    REGISTER((byte) 1, RegisterForm.class),                         // 客户端向服务端发起注册请求
    REGISTER_RESPONSE((byte) 2, RegisterResponse.class),            // 服务端响应客户端的注册
    PING((byte) 3, Ping.class),                                     // 客户端空闲时向服务端 ping
    PONG((byte) 4, Pong.class),                                     // 服务端回应客户端ping 发出 pong
    SHELL_COMMAND((byte) 5, ShellCommand.class),
    SHELL_COMMAND_RESPONSE((byte) 10, ShellCommandResponse.class),
    FILE_DESC_INFO((byte) 16, FileDescInfo.class),                  // 文件传输请求
    FILE_BURST_INSTRUCT((byte) 17, FileBurstInstruct.class),        // 文件传输指令
    FILE_BURST_DATA((byte) 18, FileBurstData.class)                 // 文件传输数据
    ;

    byte val;
    Class<?> protocolStruct;

    private MsgTypeEnum(byte val, Class<?> protocolStruct) {
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

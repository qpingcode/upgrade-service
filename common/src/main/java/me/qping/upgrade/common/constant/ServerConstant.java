package me.qping.upgrade.common.constant;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @ClassName ServerConstant
 * @Description 服务器常量
 * @Author qping
 * @Date 2021/7/2 08:39
 * @Version 1.0
 **/
public class ServerConstant {

    public static final String MSG_PROTOCAL_ID = "Rx";      // 协议定义消息开头
    public static final int SERVER_NODE_ID = 0;             // server 的 workId， 用于生成消息id

    public static final String Host = "127.0.0.1";
    public static final int Port = 52000;

    public static final int LengthFieldLength = 4;
    public static final int MaxFrameLength = Integer.MAX_VALUE;

    public static final int IdleThenClose = 60;             // 服务器端判断客户端60s没有数据传过来就关闭channel
    public static final int IdleThenPing = 5;               // 客户端判断 5s 没有交互，就发一个心跳


    public static final int MSG_STORAGE_MSG_TIMEOUT = 300 * 1000;
    public static final int MSG_STORAGE_CLEAN_SLEEP_INTERVAL = 30 * 1000;



}

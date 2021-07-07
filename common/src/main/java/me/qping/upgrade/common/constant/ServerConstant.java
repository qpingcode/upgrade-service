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

    public static final int RequestTimeout = 30*1000;       // 请求超时时间

    public static final String Host = "127.0.0.1";
    public static final int Port = 52000;

    public static final int LengthFieldLength = 4;
    public static final int MaxFrameLength = Integer.MAX_VALUE;

    public static final int IdleTimeoutThenClose = 20;        // 服务器端判断 20s 没有数据传过来就关闭
    public static final int IdleThenPing = 5;                 // 客户端判断 5s 没有交互，就发一个心跳


}

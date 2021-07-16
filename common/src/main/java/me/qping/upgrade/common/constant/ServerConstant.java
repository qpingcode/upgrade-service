package me.qping.upgrade.common.constant;

import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.Charset;

/**
 * @ClassName ServerConstant
 * @Description 服务器常量
 * @Author qping
 * @Date 2021/7/2 08:39
 * @Version 1.0
 **/
public class ServerConstant {

    public static final String MSG_PROTOCAL_ID = "Rx";                                          // 协议定义消息开头
    public static final Charset MSG_PROTOCAL_CHARSET = Charset.forName("utf8");                 // 协议定义消息字符集
    public static final int MSG_PROTOCAL_ID_LENGTH = ByteBufUtil.utf8Bytes(MSG_PROTOCAL_ID);    // 协议定义消息开头长度

    public static final long SERVER_NODE_ID = 0;             // server 的 workId， 用于生成消息id

    public static final String Host = "127.0.0.1";
    public static final int Port = 52000;

    public static final int LengthFieldLength = 4;
    public static final int MaxFrameLength = Integer.MAX_VALUE;

    public static final int IdleThenClose = 60;             // 服务器端判断客户端60s没有数据传过来就关闭channel
    public static final int IdleThenPing = 5;               // 客户端判断 5s 没有交互，就发一个心跳



    public static final int DEFAULT_CHUCK_SIZE = 1024 * 100; // 默认分块100K
    public static final int MIN_CHUCK_SIZE = 1024 * 10;     // 最小分块 1K， 不能比这个小



    public static final int MSG_STORAGE_MSG_TIMEOUT = 60 * 1000;
    public static final int MSG_STORAGE_CLEAN_SLEEP_INTERVAL = 30 * 1000;


    public static final String JdbcUrl = "jdbc:h2:file:/Users/qping/test/.h2/upgrade_server_db;AUTO_SERVER=TRUE";
    public static final String JdbcUsername = "sa";
    public static final String JdbcPassword = "";
}

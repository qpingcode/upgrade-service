package me.qping.upgrade.common.constant;

import lombok.Data;

/**
 * @ClassName Response
 * @Description 返回
 * @Author qping
 * @Date 2021/7/2 09:12
 * @Version 1.0
 **/
public interface ResponseCode {

    // 成功
    int SUCCESS = 1;


    // 通用异常
    int ERR_OTHER = -1;                  // 其他错误
    int ERR_FILE_NOT_EXISTS = -6;

    // 服务端异常
    int ERR_REG_REPEAT = -102;           // 重复注册
    int ERR_CLIENT_ID_ILLEGAL = -103;    // nodeId 非法
    int ERR_CLIENT_OFFLINE = -104;       // 客户端下线

    // 客户端异常
    int ERR_COMMAND_ERROR = -205;

}

package me.qping.upgrade.common.message.impl;

import lombok.Data;

/**
 * @ClassName Response
 * @Description 返回
 * @Author qping
 * @Date 2021/7/2 09:12
 * @Version 1.0
 **/
public interface Response {

    // 成功
    int SUCCESS = 1;

    // 请求超时
    int ERR_TIMEOUT = -1;

    // 其他错误
    int ERR_OTHER = -999;


    // 重复注册
    int ERR_REG_REPEAT = 2;

    // clientId 非法
    int ERR_CLIENT_ID_ILLEGAL = 3;


    // client 下线
    int ERR_CLIENT_OFFLINE = 4;


    int ERR_COMMAND_ERROR = 5;


    int ERR_FILE_NOT_EXISTS = 6;


    long getCode();
    String getMessage();

    void setCode(long code);
    void setMessage(String msg);

}

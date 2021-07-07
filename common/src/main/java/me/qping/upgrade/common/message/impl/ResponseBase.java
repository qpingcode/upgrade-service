package me.qping.upgrade.common.message.impl;

import lombok.Data;

/**
 * @ClassName Response
 * @Description TODO
 * @Author qping
 * @Date 2021/7/2 09:12
 * @Version 1.0
 **/
@Data
public class ResponseBase implements Response{
    long code;
    String message;

    public static Response timeout(){
        ResponseBase res = new ResponseBase();
        res.setCode(Response.ERR_TIMEOUT);//响应异常处理
        res.setMessage("请求超时！");
        return res;
    }
}

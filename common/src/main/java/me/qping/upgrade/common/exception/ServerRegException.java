package me.qping.upgrade.common.exception;

/**
 * @ClassName ServerRegException
 * @Description 注册客户端异常
 * @Author qping
 * @Date 2021/7/7 15:48
 * @Version 1.0
 **/
public class ServerRegException extends Exception{

    int responseCode;

    public ServerRegException(int responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public int getResponseCode(){
        return responseCode;
    }

}

package me.qping.upgrade.common.message;

import lombok.Data;
import me.qping.upgrade.common.constant.MsgType;

import java.io.Serializable;

@Data
public class Msg implements Serializable {

    private static final long serialVersionUID = 1L;

    // 类型
    byte type;

    // 消息id
    long messageId;

    // 客户端 id
    long clientId;

    // 内容
    Object body;


    public static Msg register(){
        Msg message = new Msg();
        message.setType(MsgType.REGISTER);
        return message;
    }

    public static <T> Msg registerResponse(long messageId, T body){
        Msg message = new Msg();
        message.setType(MsgType.REGISTER_RESPONSE);
        message.setMessageId(messageId);
        message.setBody(body);
        return message;
    }

    public static Msg ping(){
        Msg message = new Msg();
        message.setType(MsgType.PING);
        return message;
    }

    public static Msg pong(){
        Msg message = new Msg();
        message.setType(MsgType.PONG);
        return message;
    }

    public static <T> Msg request(long messageId, T body){
        Msg m = new Msg();
        m.setType(MsgType.REQUEST);
        m.setBody(body);
        return m;
    }

    public static <T> Msg response(long messageId, T body){
        Msg m = new Msg();
        m.setMessageId(messageId);
        m.setType(MsgType.RESPONSE);
        m.setBody(body);
        return m;
    }

}
package me.qping.upgrade.common.message;

import lombok.Data;
import me.qping.upgrade.common.constant.MsgType;
import me.qping.upgrade.common.message.codec.MessagePackUtil;
import me.qping.upgrade.common.message.impl.RegisterForm;

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
    byte[] body;


    public static Msg register(){
        Msg message = new Msg();
        message.setType(MsgType.REGISTER);
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
        byte[] message = MessagePackUtil.toBytes(body);
        Msg m = new Msg();
        m.setType(MsgType.REQUEST);
        m.setBody(message);
        return m;
    }

    public static <T> Msg response(long messageId, T body){
        byte[] message = MessagePackUtil.toBytes(body);
        Msg m = new Msg();
        m.setMessageId(messageId);
        m.setType(MsgType.RESPONSE);
        m.setBody(message);
        return m;
    }

}
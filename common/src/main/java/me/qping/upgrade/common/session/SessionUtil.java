package me.qping.upgrade.common.session;

import io.netty.channel.Channel;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName SessionUtil
 * @Description session管理器
 * @Author qping
 * @Date 2021/7/7 16:32
 * @Version 1.0
 **/
public class SessionUtil {

    public static final ConcurrentHashMap<Long, Channel> clientChannelMap = new ConcurrentHashMap<>();

    public static void bindSession(Session session, Channel channel) {
        channel.attr(Attributes.SESSION).set(session);

        if(session == null){
            return;
        }
        session.setCreateDate(new Date());
        channel.attr(Attributes.SESSION).set(session);
        clientChannelMap.put(session.getClientId(), channel);

        System.err.println("客户端上线：" + session.getClientId() + "，时间：" + new Date());

    }

    public static void unBindSession(Channel channel) {
        if (hasLogin(channel)) {
            Session session = getSession(channel);
            clientChannelMap.remove(session.getClientId());
            channel.attr(Attributes.SESSION).set(null);
            System.err.println("客户端下线：" + session.getClientId() + "，时间：" + new Date());
        }
    }

    private static boolean hasLogin(Channel channel) {
        return channel.hasAttr(Attributes.SESSION);
    }

    private static Session getSession(Channel channel) {
        return channel.attr(Attributes.SESSION).get();
    }

    public static Session getSession(Long clientId) {
        Channel channel = clientChannelMap.get(clientId);
        if(channel == null){
            return null;
        }

        return channel.attr(Attributes.SESSION).get();
    }


    public static Map<Long, Channel> getClientChannelMap() {
        return clientChannelMap;
    }

}

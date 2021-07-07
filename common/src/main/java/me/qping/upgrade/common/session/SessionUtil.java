package me.qping.upgrade.common.session;

import io.netty.channel.Channel;
import me.qping.upgrade.common.constant.MsgType;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.SnowFlakeId;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.qping.upgrade.common.constant.ServerConstant.SERVER_WORK_ID;
import static me.qping.upgrade.common.message.impl.Response.ERR_COMMAND_CLIENT_OFFLINE;

/**
 * @ClassName SessionUtil
 * @Description session管理器
 * @Author qping
 * @Date 2021/7/7 16:32
 * @Version 1.0
 **/
public class SessionUtil {

    public static final ConcurrentHashMap<Long, Channel> clientChannelMap = new ConcurrentHashMap<>();
    public static SnowFlakeId messageIdGen = new SnowFlakeId(SERVER_WORK_ID,  1);

    public static void main(String[] args) {
        SnowFlakeId messageIdGen = new SnowFlakeId(0,  1);
        SnowFlakeId messageIdGen2 = new SnowFlakeId(1,  1);

        System.out.println(messageIdGen.nextId() + " " + messageIdGen2.nextId());
    }


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

    public static Channel getChannel(long clientId){
        return clientChannelMap.get(clientId);
    }

    public static Map<Long, Channel> getClientChannelMap() {
        return clientChannelMap;
    }


    /**
     * 传输文件到客户端
     * @param session
     * @param file
     */
    public static void transferFile(Session session, String file){

    }

    /**
     * 在客户端执行Shell脚本
     * @param clientId
     * @param command
     */
    public static void executeShell(long clientId, String command) throws ServerException {

        Msg msg = new Msg();
        msg.setMessageId(messageIdGen.nextId());
        msg.setType(MsgType.SHELL_COMMAND);
        msg.setBody(command);

        Channel channel = getChannel(clientId);
        if(channel == null){
            throw new ServerException(ERR_COMMAND_CLIENT_OFFLINE, "客户端已下线" + clientId);
        }

        channel.writeAndFlush(msg);
    }


}

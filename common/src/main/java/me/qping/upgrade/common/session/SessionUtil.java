package me.qping.upgrade.common.session;

import io.netty.channel.Channel;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.MsgStorage;
import me.qping.upgrade.common.message.SnowFlakeId;
import me.qping.upgrade.common.message.handler.FileTransferUtil;
import me.qping.upgrade.common.message.impl.FileDescInfo;
import me.qping.upgrade.common.message.impl.ShellCommand;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.qping.upgrade.common.constant.ResponseCode.ERR_CLIENT_OFFLINE;
import static me.qping.upgrade.common.constant.ResponseCode.ERR_FILE_NOT_EXISTS;
import static me.qping.upgrade.common.constant.ServerConstant.SERVER_NODE_ID;

/**
 * @ClassName SessionUtil
 * @Description session管理器
 * @Author qping
 * @Date 2021/7/7 16:32
 * @Version 1.0
 **/
public class SessionUtil {

    public static final ConcurrentHashMap<Long, Channel> nodeChannelMap = new ConcurrentHashMap<>();
    public static SnowFlakeId messageIdGen = new SnowFlakeId(SERVER_NODE_ID,  1);

    public static void main(String[] args) {
        SnowFlakeId messageIdGen = new SnowFlakeId(0,  1);
        SnowFlakeId messageIdGen2 = new SnowFlakeId(1,  1);

        System.out.println(messageIdGen.nextId() + " " + messageIdGen2.nextId());
    }


    public static void bindSession(Session session, Channel channel) {
        if(session == null){
            return;
        }
        session.setCreateDate(new Date());
        channel.attr(Attributes.SESSION).set(session);
        nodeChannelMap.put(session.getNodeId(), channel);

        System.err.println("客户端上线：" + session.getNodeId() + "，时间：" + new Date());

    }

    public static void unBindSession(Channel channel) {
        if (hasLogin(channel)) {
            Session session = getSession(channel);
            nodeChannelMap.remove(session.getNodeId());
            channel.attr(Attributes.SESSION).set(null);
            System.err.println("客户端下线：" + session.getNodeId() + "，时间：" + new Date());
        }
    }

    public static boolean hasLogin(Channel channel) {
        return channel != null && channel.hasAttr(Attributes.SESSION);
    }

    public static Session getSession(Channel channel) {
        if(!hasLogin(channel)){
            return null;
        }

        return channel.attr(Attributes.SESSION).get();
    }

    public static Session getSession(Long nodeId) {
        Channel channel = nodeChannelMap.get(nodeId);
        if(channel == null){
            return null;
        }

        return channel.attr(Attributes.SESSION).get();
    }

    public static Channel getChannel(long nodeId){
        return nodeChannelMap.get(nodeId);
    }

    public static Map<Long, Channel> getNodeChannelMap() {
        return nodeChannelMap;
    }


    /**
     * 在客户端执行Shell脚本
     * @param nodeId
     * @param command
     */
    public static long executeShell(long nodeId, String command) throws ServerException {

        long messageId = messageIdGen.nextId();

        ShellCommand msg = new ShellCommand(command);
        msg.setMessageId(messageId);

        Channel channel = getChannel(nodeId);
        if(channel == null){
            throw new ServerException(ERR_CLIENT_OFFLINE, "客户端已下线" + nodeId);
        }

        channel.writeAndFlush(msg);
        MsgStorage.init(messageId);
        return messageId;
    }

    /**
     * 下发文件到客户端
     * @param nodeId          客户端id
     * @param serverFilePath    服务器文件路径
     */
    public static void transferTo(long nodeId, String serverFilePath) throws ServerException {
        File file = new File(serverFilePath);

        if(!file.exists()){
            throw new ServerException(ERR_FILE_NOT_EXISTS, "文件不存在：" + serverFilePath);
        }


        FileDescInfo fileDescInfo = FileTransferUtil.buildRequestTransferFile(messageIdGen.nextId(),
                file.getAbsolutePath(), file.getName(), "", file.length());

        Channel channel = getChannel(nodeId);
        if(channel == null){
            throw new ServerException(ERR_CLIENT_OFFLINE, "客户端已下线" + nodeId);
        }

        System.out.println("开发下发文件：" + serverFilePath + "，到客户端：" + nodeId );

        channel.writeAndFlush(fileDescInfo);
    }


    /**
     * 从客户端上传文件到服务器
     * @param nodeId          客户端id
     * @param clientFilePath    客户端文件路径
     */
    public static void transferFrom(long nodeId, String clientFilePath) throws ServerException {

        // todo 断点续传信息，实际应用中需要将断点续传信息保存到数据库中
        // todo 文件大小需传进来
        Msg sendFileTransferProtocol = FileTransferUtil.buildTransferInstruct(messageIdGen.nextId(), FileStatus.BEGIN, clientFilePath, 0l, -1l);

        Channel channel = getChannel(nodeId);
        if(channel == null){
            throw new ServerException(ERR_CLIENT_OFFLINE, "客户端已下线" + nodeId);
        }

        System.out.println("开始接收文件：" + clientFilePath);
        channel.writeAndFlush(clientFilePath);

    }
}

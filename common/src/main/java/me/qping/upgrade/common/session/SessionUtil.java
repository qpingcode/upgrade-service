package me.qping.upgrade.common.session;

import io.netty.channel.Channel;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.constant.ResponseCode;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.MsgStorage;
import me.qping.upgrade.common.message.SnowFlakeId;
import me.qping.upgrade.common.message.handler.FileProgressHandler;
import me.qping.upgrade.common.message.impl.*;
import me.qping.upgrade.common.message.progress.ProgressStorage;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.qping.upgrade.common.constant.ResponseCode.ERR_NODE_OFFLINE;
import static me.qping.upgrade.common.constant.ResponseCode.ERR_FILE_NOT_EXISTS;
import static me.qping.upgrade.common.constant.ServerConstant.DEFAULT_CHUCK_SIZE;
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
        return channel != null && channel.hasAttr(Attributes.SESSION) && channel.attr(Attributes.SESSION).get() != null;
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

    public static long getMessageId(){
        return messageIdGen.nextId();
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
            throw new ServerException(ERR_NODE_OFFLINE, "客户端已下线" + nodeId);
        }
        MsgStorage.init(messageId);
        channel.writeAndFlush(msg);

        return messageId;
    }


    public static long askFile(long nodeId, String filePath) throws ServerException {

        long messageId = messageIdGen.nextId();

        FileAsk msg = new FileAsk(filePath);
        msg.setMessageId(messageId);

        Channel channel = getChannel(nodeId);
        if(channel == null){
            throw new ServerException(ERR_NODE_OFFLINE, "客户端已下线" + nodeId);
        }

        MsgStorage.init(messageId);
        channel.writeAndFlush(msg);

        return messageId;
    }


    /**
     * 下发文件到客户端
     * @param nodeId          客户端id
     * @param serverFilePath    服务器文件路径
     * @param targetPath
     */
    public static int transferTo(long nodeId, String serverFilePath, String targetPath, boolean breakPointResume) throws ServerException {
        File file = new File(serverFilePath);

        if(!file.exists()){
            throw new ServerException(ERR_FILE_NOT_EXISTS, "文件不存在：" + serverFilePath);
        }

        ProgressStorage storage = ProgressStorage.getInstance();
        FileProgress progress = null;
        long messageId = messageIdGen.nextId();

        if(breakPointResume){
            try {
                progress = storage.findByNodeIdAndFilePathAndFileName(SERVER_NODE_ID, file.getAbsolutePath(), file.getName());
                if (null != progress) {
                    progress.clearDataAndPrepareToRead();
                    progress.setMessageId(messageId);
                    progress.setChunkSize(DEFAULT_CHUCK_SIZE);
                }
            } catch (Exception e) {
                throw new ServerException(ResponseCode.ERR_PROGRESS_QUERY, e.getMessage());
            }
        }

        if(progress == null){
            progress = new FileProgress();
            progress.setMessageId(messageId);
            progress.setNodeId(SERVER_NODE_ID);
            progress.setSourcePath(file.getAbsolutePath());
            progress.setTotalSize(file.length());
            progress.setFileName(file.getName());
            progress.setTargetPath(targetPath);
            progress.setChunkSize(DEFAULT_CHUCK_SIZE);
            progress.setReadPosition(0);
            progress.setStatus(FileStatus.CENTER);

            progress.clearDataAndPrepareToRead();

            try {
                storage.insert(progress, nodeId);
            } catch (SQLException e) {
                throw new ServerException(ResponseCode.ERR_PROGRESS_INSERT, e.getMessage());
            }
        }

        Channel channel = getChannel(nodeId);
        if(channel == null){
            throw new ServerException(ERR_NODE_OFFLINE, "客户端已下线" + nodeId);
        }

        FileProgress writeProgress = FileProgressHandler.readData(progress);
        channel.writeAndFlush(writeProgress);
        System.out.println("开始下发文件：" + serverFilePath + "，到客户端：" + nodeId );
        return writeProgress.getId();
    }


    /**
     * 从客户端上传文件到服务器
     * @param nodeId          客户端id
     * @param clientFilePath  客户端文件路径（必须到文件名）
     * @param clientFileSize  客户端大小字节数
     * @param targetPath      服务器存储路径（必须到文件名）
     * @param breakPointResume  是否断点续传
     */
    public static int transferFrom(long nodeId, String clientFilePath, long clientFileSize, String clientFileName, String targetPath, boolean breakPointResume) throws ServerException {

        long messageId = messageIdGen.nextId();
        ProgressStorage storage = ProgressStorage.getInstance();
        FileProgress progress = null;
        if(breakPointResume){
            try {
                progress = storage.findByNodeIdAndFilePathAndFileName(nodeId, clientFilePath, clientFileName);
                if (null != progress) {
                    progress.clearDataAndPrepareToRead();
                    progress.setMessageId(messageId);
                    progress.setChunkSize(DEFAULT_CHUCK_SIZE);
                }
            } catch (Exception e) {
                throw new ServerException(ResponseCode.ERR_PROGRESS_QUERY, e.getMessage());
            }
        }

        if(progress == null){
            progress = new FileProgress();
            progress.setMessageId(messageId);
            progress.setNodeId(nodeId);

            progress.setSourcePath(clientFilePath);
            progress.setTotalSize(clientFileSize);
            progress.setFileName(clientFileName);

            progress.setTargetPath(targetPath);
            progress.setChunkSize(DEFAULT_CHUCK_SIZE);
            progress.setReadPosition(0);
            progress.setStatus(FileStatus.CENTER);

            progress.clearDataAndPrepareToRead();

            try {
                storage.insert(progress, SERVER_NODE_ID);
            } catch (SQLException e) {
                throw new ServerException(ResponseCode.ERR_PROGRESS_INSERT, e.getMessage());
            }
        }


        Channel channel = getChannel(nodeId);
        if(channel == null){
            throw new ServerException(ERR_NODE_OFFLINE, "客户端已下线" + nodeId);
        }

        channel.writeAndFlush(progress);

        System.out.println("开始接收文件：" + clientFilePath);
        return progress.getId();


    }



}

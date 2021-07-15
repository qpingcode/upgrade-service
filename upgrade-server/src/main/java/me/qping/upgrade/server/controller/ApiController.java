package me.qping.upgrade.server.controller;

import io.netty.channel.Channel;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.MsgStorage;
import me.qping.upgrade.common.message.impl.FileAskResponse;
import me.qping.upgrade.common.message.impl.ForceOffline;
import me.qping.upgrade.common.message.impl.ShellCommandResponse;
import me.qping.upgrade.common.session.Session;
import me.qping.upgrade.common.session.SessionUtil;
import me.qping.upgrade.server.netty.UpgradeServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName IndexController
 * @Description
 * @Author qping
 * @Date 2021/7/5 16:12
 * @Version 1.0
 **/
@Controller
public class ApiController {

    @Autowired
    UpgradeServer upgradeServer;

    /**
     * 展示客户端连接列表
     * @return
     */
    @RequestMapping(value = "/node/list")
    @ResponseBody
    public List<Session> nodeList(){
        List<Session> list = new ArrayList<>();

        for(Long nodeId : SessionUtil.getNodeChannelMap().keySet()){
            Session session = SessionUtil.getSession(nodeId);
            list.add(session);
        }
        return list;
    }

    /**
     * 将客户端踢下线
     * @param nodeId
     * @return
     */
    @RequestMapping(value = "/node/kick")
    @ResponseBody
    public boolean kickNode(long nodeId){
        Channel channel = SessionUtil.getChannel(nodeId);

        ForceOffline cmd = new ForceOffline();
        cmd.setMessageId(SessionUtil.getMessageId());

        if(channel == null){
            return false;
        }
        System.out.println("强制下线客户端：" + nodeId);
        channel.writeAndFlush(cmd);
        return true;
    }

    /**
     * 在客户端上执行shell
     * @param nodeId
     * @param shell
     * @return
     */
    @RequestMapping(value = "/node/executeShell")
    @ResponseBody
    public ShellCommandResponse executeShell(long nodeId, String shell){
        try {

            long messageId = SessionUtil.executeShell(nodeId, shell);

            ShellCommandResponse result = MsgStorage.get(messageId, 10 * 1000);
            return result;

        } catch (ServerException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 服务器下发文件
     * @param nodeId
     * @param sourcePath    服务器文件路径
     * @param targetPath    客户端存储路径
     */
    @RequestMapping(value = "/node/transferTo")
    @ResponseBody
    public void transferTo(long nodeId, String sourcePath, String targetPath){
        try {
            SessionUtil.transferTo(nodeId, sourcePath, targetPath, true);
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务器从客户端获取文件
     * @param nodeId
     * @param sourcePath    客户端文件路径
     * @param targetPath    服务器存储路径
     */
    @RequestMapping(value = "/node/transferFrom")
    @ResponseBody
    public void transferFrom(long nodeId, String sourcePath, String targetPath){
        try {

            long messageId = SessionUtil.askFile(nodeId, sourcePath);

            FileAskResponse result = MsgStorage.get(messageId, 10 * 1000);
            if(result == null){
                System.err.println("无法查看的客户端的文件信息,客户端id： " + nodeId + " 文件：" +sourcePath);
                return;
            }

            SessionUtil.transferFrom(nodeId, result.getFileUrl(), result.getFileSize(), targetPath, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

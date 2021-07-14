package me.qping.upgrade.server.controller;

import io.netty.channel.Channel;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.MsgStorage;
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

    @RequestMapping(value = "/node/kick")
    @ResponseBody
    public boolean kickNode(long nodeId){
        Channel channel = SessionUtil.getChannel(nodeId);

        ForceOffline cmd = new ForceOffline();
        cmd.setMessageId(SessionUtil.getMessageId());

        if(channel == null){
            return false;
        }

        channel.writeAndFlush(cmd);
        return true;
    }

    @RequestMapping(value = "/client/executeShell")
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

    @RequestMapping(value = "/client/transferTo")
    @ResponseBody
    public void transferTo(long nodeId, String serverFilePath){
        try {
            SessionUtil.transferTo(nodeId, serverFilePath);
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/client/transferFrom")
    @ResponseBody
    public void transferFrom(long nodeId, String clientFilePath){
        try {
            SessionUtil.transferFrom(nodeId, clientFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

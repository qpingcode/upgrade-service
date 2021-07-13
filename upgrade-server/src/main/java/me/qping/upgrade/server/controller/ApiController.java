package me.qping.upgrade.server.controller;

import io.netty.channel.Channel;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.MsgStorage;
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

    @RequestMapping(value = "/client/list")
    @ResponseBody
    public List<Session> clientList(){
        List<Session> list = new ArrayList<>();

        for(Long clientId : SessionUtil.getNodeChannelMap().keySet()){
            Session session = SessionUtil.getSession(clientId);
            list.add(session);
        }
        return list;
    }

    @RequestMapping(value = "/client/kick")
    @ResponseBody
    public boolean kickClient(long clientId){
        Channel channel = SessionUtil.getNodeChannelMap().get(clientId);

        if(channel == null){
            return false;
        }

        SessionUtil.unBindSession(channel);
        channel.close();
        return true;
    }

    @RequestMapping(value = "/client/executeShell")
    @ResponseBody
    public ShellCommandResponse executeShell(long clientId, String shell){
        try {

            long messageId = SessionUtil.executeShell(clientId, shell);

            ShellCommandResponse result = MsgStorage.get(messageId, 10 * 1000);
            return result;

        } catch (ServerException e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/client/transferTo")
    @ResponseBody
    public void transferTo(long clientId, String serverFilePath){
        try {
            SessionUtil.transferTo(clientId, serverFilePath);
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/client/transferFrom")
    @ResponseBody
    public void transferFrom(long clientId, String clientFilePath){
        try {
            SessionUtil.transferFrom(clientId, clientFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

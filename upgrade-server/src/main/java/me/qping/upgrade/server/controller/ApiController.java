package me.qping.upgrade.server.controller;

import me.qping.upgrade.server.bean.ClientInfo;
import me.qping.upgrade.server.netty.UpgradeServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public List<ClientInfo> clientList(){
        return upgradeServer.clientList();

    }

    @RequestMapping(value = "/client/kick")
    @ResponseBody
    public boolean kickClient(long clientId){
        return upgradeServer.clientKick(clientId);

    }

}

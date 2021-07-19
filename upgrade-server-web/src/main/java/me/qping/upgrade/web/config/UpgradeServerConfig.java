package me.qping.upgrade.web.config;

import me.qping.upgrade.server.netty.UpgradeServer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @ClassName UpgradeServerConfig
 * @Description TODO
 * @Author qping
 * @Date 2021/7/19 14:10
 * @Version 1.0
 **/
@Component
public class UpgradeServerConfig {

    @PostConstruct
    public void autoStart() throws Exception {
        UpgradeServer server = new UpgradeServer();
        server.start();
    }

}

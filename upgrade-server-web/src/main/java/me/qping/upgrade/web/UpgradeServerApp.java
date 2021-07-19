package me.qping.upgrade.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName UpgradeApp
 * @Description 集成springboot
 * @Author qping
 * @Date 2021/7/5 16:04
 * @Version 1.0
 **/
@SpringBootApplication
@ComponentScan
public class UpgradeServerApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(UpgradeServerApp.class).run(args);
    }

}

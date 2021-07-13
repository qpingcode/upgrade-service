package me.qping.upgrade.common.session;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName Client
 * @Description 客户端
 * @Author qping
 * @Date 2021/7/5 16:27
 * @Version 1.0
 **/
@Data
public class Session {
    long nodeId;
    String address;
    Date createDate;
}

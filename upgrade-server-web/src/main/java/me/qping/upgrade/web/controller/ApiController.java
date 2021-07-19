package me.qping.upgrade.web.controller;

import cn.hutool.core.lang.Assert;
import io.netty.channel.Channel;
import me.qping.upgrade.common.constant.ServerConstant;
import me.qping.upgrade.common.exception.ServerException;
import me.qping.upgrade.common.message.MsgStorage;
import me.qping.upgrade.common.message.impl.FileAskResponse;
import me.qping.upgrade.common.message.impl.FileBean;
import me.qping.upgrade.common.message.impl.ForceOffline;
import me.qping.upgrade.common.message.impl.ShellCommandResponse;
import me.qping.upgrade.common.message.sql.FileProgressBean;
import me.qping.upgrade.common.message.sql.ProgressStorage;
import me.qping.upgrade.common.session.Session;
import me.qping.upgrade.common.session.SessionUtil;
import me.qping.upgrade.server.netty.UpgradeServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import static me.qping.upgrade.common.constant.ServerConstant.JdbcPassword;

/**
 * @ClassName IndexController
 * @Description
 * @Author qping
 * @Date 2021/7/5 16:12
 * @Version 1.0
 **/
@Controller
public class ApiController {

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

        Assert.isTrue(nodeId >= 0);

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

            Assert.notBlank(shell);
            Assert.isTrue(nodeId >= 0);

            long messageId = SessionUtil.executeShell(nodeId, shell);

            ShellCommandResponse result = MsgStorage.get(messageId, 10 * 1000);
            return result;

        } catch (ServerException e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/node/askFile")
    @ResponseBody
    public FileAskResponse askFile(long nodeId, String sourcePath){
        try {

            Assert.notBlank(sourcePath);
            Assert.isTrue(nodeId >= 0);

            long messageId = SessionUtil.askFile(nodeId, sourcePath);

            FileAskResponse result = MsgStorage.get(messageId, 10 * 1000);
            if(result == null){
                System.err.println("客户端的文件查看超时, 客户端id： " + nodeId + " 文件：" + sourcePath);
                return result;
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 服务器下发文件
     * @param nodeId
     * @param sourcePath    服务器文件路径
     * @param targetPath    客户端存储路径
     */
    @RequestMapping(value = "/transfer/to")
    @ResponseBody
    public int transferTo(long nodeId, String sourcePath, String targetPath){
        try {

            Assert.notBlank(sourcePath);
            Assert.notBlank(targetPath);
            Assert.isTrue(nodeId >= 0);

            int progressId = SessionUtil.transferTo(nodeId, sourcePath, targetPath, false);
            return progressId;
        } catch (ServerException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 服务器从客户端获取文件
     * @param nodeId
     * @param sourcePath    客户端文件路径
     * @param targetPath    服务器存储路径
     */
    @RequestMapping(value = "/transfer/from")
    @ResponseBody
    public int transferFrom(long nodeId, String sourcePath, String targetPath){
        try {

            Assert.notBlank(sourcePath);
            Assert.notBlank(targetPath);
            Assert.isTrue(nodeId >= 0);

            long messageId = SessionUtil.askFile(nodeId, sourcePath);

            FileAskResponse result = MsgStorage.get(messageId, 10 * 1000);
            if(result == null){
                System.err.println("客户端的文件查看超时, 客户端id： " + nodeId + " 文件：" +sourcePath);
                return -1;
            }

            if(result.isDir()){
                System.err.println("无法复制目录, 客户端id： " + nodeId + " 文件：" +sourcePath);
                return -1;
            }

            if(!result.isExists()){
                System.err.println("客户端的文件不存在, 客户端id： " + nodeId + " 文件：" +sourcePath);
                return -1;
            }

            FileBean f = result.getFileBeans().get(0);
            int progressId = SessionUtil.transferFrom(nodeId, f.getFilePath(), f.getFileSize(), f.getFileName(), targetPath, false);

            return progressId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @RequestMapping(value = "/transfer/stop")
    @ResponseBody
    public int transferStop(int progressId){
        try {
            SessionUtil.transferStop(progressId);

            return progressId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 传输文件列表
     */
    @RequestMapping(value = "/transfer/list")
    @ResponseBody
    public List<FileProgressBean> transferList(){
        try {

            ProgressStorage storage = ProgressStorage.getInstance();
            List<FileProgressBean> beans = storage.findAll();
            return beans;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 传输文件列表
     */
    @RequestMapping(value = "/transfer/findById")
    @ResponseBody
    public FileProgressBean transferFindById(Integer id){
        try {
            ProgressStorage storage = ProgressStorage.getInstance();
            FileProgressBean bean = storage.findById(id);
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 传输文件列表
     */
    @RequestMapping(value = "/transfer/initdb")
    @ResponseBody
    public boolean transferInitDB(){
        try {
            ProgressStorage.getInstance().init(ServerConstant.JdbcUrl, ServerConstant.JdbcUsername, JdbcPassword, true);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("初始化数据库失败：" + e.getMessage());
        }
    }


}

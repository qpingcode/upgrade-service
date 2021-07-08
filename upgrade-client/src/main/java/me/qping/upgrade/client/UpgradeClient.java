package me.qping.upgrade.client;

import cn.hutool.core.io.resource.ClassPathResource;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import me.qping.upgrade.client.handler.ClientOnlineHandler;
import me.qping.upgrade.common.constant.ServerConstant;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.SnowFlakeId;
import me.qping.upgrade.common.message.codec.ObjDecoder;
import me.qping.upgrade.common.message.codec.ObjEncoder;
import me.qping.upgrade.common.message.handler.ShellCommandHandler;
import me.qping.upgrade.common.message.retry.ExponentialBackOffRetry;
import me.qping.upgrade.common.message.retry.RetryPolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.qping.upgrade.common.constant.ServerConstant.*;

/**
 * @ClassName NettyClient
 * @Description 客户端
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class UpgradeClient implements Client {

    AtomicBoolean stopped = new AtomicBoolean(false);
    AtomicBoolean online = new AtomicBoolean(false);

    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    Channel channel;

    RetryPolicy retryPolicy = new ExponentialBackOffRetry(2000, Integer.MAX_VALUE);
    SnowFlakeId idGen;

    int retries = 0;
    long clientId;
    String installDir;


    public long getClientId(){
        return clientId;
    }

    @Override
    public long getMessageId() {
        return idGen.nextId();
    }

    @Override
    public boolean isOnline() {
        return online.get();
    }

    @Override
    public void setOnline(boolean flag) {
        online.set(flag);
    }

    @Override
    public void sendMsg(Msg msg) {
        if(stopped.get()){
            throw new RuntimeException("客户端已停止工作");
        }

        if(!online.get()){
            throw new RuntimeException("客户端不在线");
        }

        if(channel == null || !channel.isActive()){
            throw new RuntimeException("客户端通道中断");
        }

        channel.writeAndFlush(msg);
    }

    public static Properties readConfig(){
//        java.net.URL url = UpgradeClient.class.getProtectionDomain().getCodeSource().getLocation();
//        String path = null;
//        try {
//            path = java.net.URLDecoder.decode(url.getPath(), "utf-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        int lastIndex = path.lastIndexOf("/") + 1;
//        path = path.substring(0, lastIndex);

        ClassPathResource resource = new ClassPathResource("config");
        Properties properties = new Properties();
        try {
            properties.load(resource.getStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("==========================");
        System.out.println("读取配置文件config");
        for (Object key : properties.keySet()) {
            System.out.println(key + " : " + properties.get(key));
        }
        System.out.println("==========================");
        return properties;
    }

    public void initClient() {

        Properties properties = readConfig();

        clientId = Long.parseLong(properties.getProperty("clientId"));
        installDir = properties.getProperty("installDir");

        if(clientId < 1 || clientId > SnowFlakeId.maxWorkerId ){
            throw new RuntimeException("客户端ID必须大于0且小于" + (SnowFlakeId.maxWorkerId + 1));
        }

        idGen = new SnowFlakeId(clientId,  1);

    }

    public static void main(String[] args) {
        // 如果要保持客户端始终在线，就不能在有异常后 close，close以后重连就会停止
        try {
            new UpgradeClient().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {

        initClient();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(0,0, IdleThenPing));
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(MaxFrameLength, 0, LengthFieldLength, 0, LengthFieldLength));
                        ch.pipeline().addLast(new LengthFieldPrepender(LengthFieldLength));
                        ch.pipeline().addLast("decoder", new ObjDecoder(Msg.class));
                        ch.pipeline().addLast("encoder", new ObjEncoder(Msg.class));
                        ch.pipeline().addLast(new ClientOnlineHandler("客户端：" + clientId , UpgradeClient.this));
                        ch.pipeline().addLast(new ShellCommandHandler(UpgradeClient.this, true));
                    }

                });

        connect();
    }


    @Override
    public void disconnect() {
        stopped.set(true);

        if(channel != null && channel.isActive()){
            channel.close();
        }

        if(group != null) {
            group.shutdownGracefully();
        }
    }

    public void connect() {

        if (channel != null && channel.isActive()) {
            return;
        }

        if(stopped.get()){
            return;
        }

        try {
            synchronized (bootstrap) {
                final ChannelFuture cf = bootstrap.connect(ServerConstant.Host, ServerConstant.Port);
                cf.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture connect) throws Exception {
                        if (connect.isSuccess()) {
                            System.out.println(String.format("connect success."));
                            retries = 0;
                            channel = connect.channel();
                        } else {

                            if (retries == 0) {
                                System.err.println("Lost the TCP connection with the server.");
                            }

                            boolean allowRetry = getRetryPolicy().allowRetry(retries);
                            if (allowRetry) {

                                long sleepTimeMs = getRetryPolicy().getSleepTimeMs(retries);

                                System.out.println(String.format("Try to reconnect to the server after %dms. Retry count: %d.", sleepTimeMs, ++retries));

                                final EventLoop eventLoop = connect.channel().eventLoop();
                                eventLoop.schedule(() -> {
                                    connect();
                                }, sleepTimeMs, TimeUnit.MILLISECONDS);
                            }

                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("连接失败：" + e.getMessage());
        }
    }


    @Override
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
}

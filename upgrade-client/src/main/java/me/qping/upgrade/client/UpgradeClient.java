package me.qping.upgrade.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import me.qping.upgrade.client.handler.NettyClientHandler;
import me.qping.upgrade.common.constant.ServerConstant;
import me.qping.upgrade.common.message.Client;
import me.qping.upgrade.common.message.SnowFlakeId;
import me.qping.upgrade.common.message.codec.MsgPackDecode;
import me.qping.upgrade.common.message.codec.MsgPackEncode;
import me.qping.upgrade.common.message.retry.ExponentialBackOffRetry;
import me.qping.upgrade.common.message.retry.RetryPolicy;

import java.io.*;
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
    private int retries = 0;
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    private Channel channel;
    RetryPolicy retryPolicy = new ExponentialBackOffRetry(2000, Integer.MAX_VALUE);
    static long clientId;
    static SnowFlakeId idGen;

    public long getClientId(){
        return clientId;
    }

    @Override
    public long getMessageId() {
        return idGen.nextId();
    }

    public static void initClient() throws Exception {
        java.net.URL url = UpgradeClient.class.getProtectionDomain().getCodeSource().getLocation();
        String path = null;
        try {
            path = java.net.URLDecoder.decode(url.getPath(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int lastIndex = path.lastIndexOf("/") + 1;
        path = path.substring(0, lastIndex);

        File file = new File(path + "clientId");
        System.out.println("ID文件: " + file.getAbsolutePath());

        BufferedReader br = new BufferedReader(new FileReader(file));
        String id = br.readLine();
        System.out.println("ID: " + id);

        long clientIdNum = Long.parseLong(id);
        clientId = clientIdNum;
        idGen = new SnowFlakeId(clientIdNum,  1);
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
                        ch.pipeline().addLast("decoder", new MsgPackDecode());
                        ch.pipeline().addLast("encoder", new MsgPackEncode());
                        ch.pipeline().addLast(new NettyClientHandler(clientId + "", UpgradeClient.this));
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

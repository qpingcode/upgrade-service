package me.qping.upgrade.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import me.qping.upgrade.common.constant.ServerConstant;
import me.qping.upgrade.common.message.codec.ObjDecoder;
import me.qping.upgrade.common.message.codec.ObjEncoder;
import me.qping.upgrade.common.message.codec.Serialization;
import me.qping.upgrade.common.message.handler.*;
import me.qping.upgrade.common.message.impl.FileProgressListener;
import me.qping.upgrade.common.message.progress.ProgressStorage;
import me.qping.upgrade.server.netty.handler.ServerOnlineHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static me.qping.upgrade.common.constant.ServerConstant.*;


@Component
public class UpgradeServer {

    ServerBootstrap bootstrap = new ServerBootstrap();
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    ChannelFuture channelFuture;

    public void start(){

        initServer();

        try{
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(IdleThenClose,0,0));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(MaxFrameLength, 0, LengthFieldLength, 0, LengthFieldLength));
                            ch.pipeline().addLast(new LengthFieldPrepender(LengthFieldLength));
                            ch.pipeline().addLast(new ObjDecoder());
                            ch.pipeline().addLast(new ObjEncoder());
                            ch.pipeline().addLast(new ServerOnlineHandler("中心端"));
                            ch.pipeline().addLast(new SecurityHandler());
                            ch.pipeline().addLast(new ShellCommandResponseHandler());
                            ch.pipeline().addLast(new FileDescHandler("/Users/qping/Desktop/data/server"));
                            ch.pipeline().addLast(new FileProgressHandler("/Users/qping/Desktop/data/server"));
                        }
                    });

            channelFuture = bootstrap.bind(ServerConstant.Port).sync();
            channelFuture.addListener(new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("启动成功，监听端口：" + ServerConstant.Port);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initServer() {
        Serialization.init();

        try {
            ProgressStorage.getInstance().init(ServerConstant.JdbcUrl, ServerConstant.JdbcUsername, JdbcPassword);
        } catch (Exception e) {
            throw new RuntimeException("初始化数据库失败：" + e.getMessage());
        }

        ProgressStorage storage = ProgressStorage.getInstance();
        FileProgressHandler.addListener(new FileProgressListener() {
            @Override
            public void end(int progressId, long fileNodeId, String sourceUrl) {
                System.out.println(String.format("文件传输成功，源路径：%s 源节点：%s", sourceUrl, fileNodeId));
                storage.tagEnd(progressId);
            }

            @Override
            public void progress(int progressId, long fileNodeId, String sourceUrl, long totalSize, long position) {
                storage.tagProgress(progressId, totalSize, position);
            }

            @Override
            public void error(int progressId, long fileNodeId, String sourceUrl, String errorMsg) {
                System.err.println(String.format("文件传输出错啦，源路径：%s 源节点：%s 错误信息：%s", sourceUrl, fileNodeId, errorMsg));
                storage.tagError(progressId, errorMsg);
            }
        });

    }

    public void close(){
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void main( String[] args ) {
        new UpgradeServer().start();
    }


    @PostConstruct
    public void autoStart() throws Exception {
        UpgradeServer server = new UpgradeServer();
        server.start();
    }

}

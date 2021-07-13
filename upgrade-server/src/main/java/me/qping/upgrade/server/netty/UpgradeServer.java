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
                            ch.pipeline().addLast(new FileDescInfoHandler("/Users/qping/Desktop/data/server"));
                            ch.pipeline().addLast(new FileBurstInstructHandler());
                            ch.pipeline().addLast(new FileBurstDataHandler("/Users/qping/Desktop/data/server"));
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

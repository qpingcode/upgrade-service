package me.qping.upgrade.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import me.qping.upgrade.common.session.Attributes;
import me.qping.upgrade.common.constant.ServerConstant;
import me.qping.upgrade.common.message.Msg;
import me.qping.upgrade.common.message.codec.ObjDecoder;
import me.qping.upgrade.common.message.codec.ObjEncoder;
import me.qping.upgrade.common.message.handler.AckInboundMiddleware;
import me.qping.upgrade.common.session.Session;
import me.qping.upgrade.common.session.SessionUtil;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static me.qping.upgrade.common.constant.ServerConstant.*;


@Component
public class UpgradeServer {

    ServerBootstrap bootstrap = new ServerBootstrap();
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();



    ChannelFuture channelFuture;

    public void start(){

        try{
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(IdleTimeoutThenClose,0,0));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(MaxFrameLength, 0, LengthFieldLength, 0, LengthFieldLength));
                            ch.pipeline().addLast(new LengthFieldPrepender(LengthFieldLength));
                            ch.pipeline().addLast("decoder", new ObjDecoder(Msg.class));
                            ch.pipeline().addLast("encoder", new ObjEncoder(Msg.class));
                            ch.pipeline().addLast(new ServerOnlineHandler("中心端"));
                            ch.pipeline().addLast(new AckInboundMiddleware("中心端"));
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



    /**
     * 获取所有在线客户端
     * @return
     */
    public List<Session> getSessionList() {

        List<Session> list = new ArrayList<>();

        for(Long clientId : SessionUtil.getClientChannelMap().keySet()){
            Session session = SessionUtil.getSession(clientId);
            list.add(session);
        }

        return list;
    }

    /**
     * 命令下线
     * @param clientId
     * @return
     *
     * todo 待修改，服务器下线后客户端又会重连
     */
    public boolean kickSession(long clientId){

        Channel channel = SessionUtil.getClientChannelMap().get(clientId);

        if(channel == null){
            return false;
        }

        SessionUtil.unBindSession(channel);
        channel.close();
        return true;

    }

    /**
     * 传输文件到客户端
     * @param client
     * @param file
     */
    public void transferFile(Session client, String file){

    }

    /**
     * 在客户端执行Shell脚本
     * @param client
     * @param command
     */
    public void executeShell(Session client, String command){

    }

}

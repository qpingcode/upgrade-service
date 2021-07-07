package me.qping.upgrade.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import me.qping.upgrade.common.constant.ServerConstant;
import me.qping.upgrade.common.message.codec.MsgPackDecode;
import me.qping.upgrade.common.message.codec.MsgPackEncode;
import me.qping.upgrade.server.bean.ClientInfo;
import me.qping.upgrade.server.netty.handler.NettyServerHandler;
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

    public static final ConcurrentHashMap<Long, Channel> clientChannelMap = new ConcurrentHashMap<>();

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
                            ch.pipeline().addLast("decoder", new MsgPackDecode());
                            ch.pipeline().addLast("encoder", new MsgPackEncode());
                            ch.pipeline().addLast(new NettyServerHandler("中心端"));
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

    public static void attachClient(Channel channel, ClientInfo c){

        if(c == null){
            return;
        }

        c.setCreateDate(new Date());

        AttributeKey<ClientInfo> key = AttributeKey.valueOf(ChannelAttrKeyClient);
        channel.attr(key).set(c);

        clientChannelMap.put(c.getId(), channel);
    }

    public static ClientInfo getClient(long clientId){
        Channel channel = clientChannelMap.get(clientId);

        if(channel == null || !channel.isActive()){
            return null;
        }

        AttributeKey<ClientInfo> key = AttributeKey.valueOf(ChannelAttrKeyClient);
        if(!channel.hasAttr(key)){
            return null;
        }

        return channel.attr(key).get();
    }

    /**
     * 获取所有在线客户端
     * @return
     */
    public List<ClientInfo> clientList() {

        List<ClientInfo> list = new ArrayList<>();

        for(Long clientId : clientChannelMap.keySet()){
            ClientInfo clientInfo = getClient(clientId);
            list.add(clientInfo);
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
    public boolean clientKick(long clientId){

        Channel channel = clientChannelMap.get(clientId);

        if(channel == null){
            return false;
        }

        if(!channel.isActive()){
            clientChannelMap.remove(channel);
            return false;
        }

        // 踢下线
        clientChannelMap.remove(channel);
        channel.close();
        return true;

    }

    /**
     * 传输文件到客户端
     * @param client
     * @param file
     */
    public void transferFile(ClientInfo client, String file){

    }

    /**
     * 在客户端执行Shell脚本
     * @param client
     * @param command
     */
    public void executeShell(ClientInfo client, String command){

    }

}

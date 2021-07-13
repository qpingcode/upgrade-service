package me.qping.upgrade.common.message.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.qping.upgrade.common.session.SessionUtil;

/**
 * @ClassName SecurityHandler
 * @Description 安全验证拦截处理器
 * @Author qping
 * @Date 2021/7/13 17:31
 * @Version 1.0
 **/
public class SecurityHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(SessionUtil.hasLogin(ctx.channel())){
            super.channelRead(ctx, msg);
        }else{
            System.out.println("拦截非法请求，请先登陆！");
        }
    }

}

package me.qping.upgrade.common.message.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.qping.upgrade.common.constant.FileStatus;
import me.qping.upgrade.common.message.impl.FileBurstInstruct;
import me.qping.upgrade.common.message.impl.FileDescInfo;
import me.qping.upgrade.common.message.progress.ProgressStorage;
import me.qping.upgrade.common.session.Session;
import me.qping.upgrade.common.session.SessionUtil;

/**
 * @ClassName NettyClient
 * @Description shell命令处理器
 * @Author qping
 * @Date 2021/6/28 17:12
 * @Version 1.0
 **/
public class FileDescInfoHandler extends SimpleChannelInboundHandler<FileDescInfo> {

    String basePath;

    ProgressStorage storage = ProgressStorage.getInstance();

    public FileDescInfoHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileDescInfo fileDescInfo) throws Exception {


        Session session = SessionUtil.getSession(ctx.channel());
        if(session == null){
            // 未注册的客户端，禁止上传文件
            return;
        }

        System.out.println("开始上传，文件名称：" + fileDescInfo.getFileName());


        // 文件接收方保存传输进度
        FileBurstInstruct burstInstructOld = storage.findByNodeIdAndFileUrl(session.getNodeId(), fileDescInfo.getFileUrl());

        if (null != burstInstructOld) {
            if (burstInstructOld.getStatus() == FileStatus.COMPLETE) {
                // todo 更新结束时间
//                ProgressStorage.getInstance().updateEndDate(burstInstructOld.getId());
            }

            //todo messageId 与数据库保存的不同的情况

            System.out.println("传输完成删除断点信息" + JSON.toJSONString(burstInstructOld));
            ctx.writeAndFlush(burstInstructOld);
            return;
        }

        FileBurstInstruct transferInstruct = FileTransferUtil.buildTransferInstruct(fileDescInfo.getMessageId(),
                FileStatus.BEGIN, fileDescInfo.getFileUrl(), 0l, fileDescInfo.getFileSize());

        ctx.writeAndFlush(transferInstruct);

    }

}
